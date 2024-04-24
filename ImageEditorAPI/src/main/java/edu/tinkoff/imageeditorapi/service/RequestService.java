package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.entity.FilterType;
import edu.tinkoff.imageeditorapi.entity.ImageMetaEntity;
import edu.tinkoff.imageeditorapi.entity.RequestEntity;
import edu.tinkoff.imageeditorapi.entity.StatusResponse;
import edu.tinkoff.imageeditorapi.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageeditorapi.kafka.producer.KafkaImageWipProducer;
import edu.tinkoff.imageeditorapi.repository.ImageMetaRepository;
import edu.tinkoff.imageeditorapi.repository.RequestRepository;
import edu.tinkoff.imageeditorapi.repository.exception.FileReadException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ImageMetaRepository imageMetaRepository;
    private final KafkaImageWipProducer imageWipProducer;
    private final MinioFileStorageService fileStorageService;

    public RequestEntity getRequest(final UUID requestId, final UUID imageId) {
        var requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new EntityNotFoundException("No request with id " + requestId);
        }

        var request = requestOpt.get();
        if (!request.getOriginalImageId().equals(imageId)) {
            throw new EntityNotFoundException("Request with id " + requestId + " has another original image id");
        }

        return request;
    }

    @Retryable(retryFor = {KafkaException.class}, maxAttempts = 2,
            backoff = @Backoff(delay = 100))
    public RequestEntity createRequest(final UUID imageId, final String username, final FilterType[] filterTypes) {
        var imageOpt = imageMetaRepository.findById(imageId);
        if (imageOpt.isEmpty() || !imageOpt.get().getAuthor().getUsername().equals(username)) {
            throw new EntityNotFoundException("No image with id " + imageId);
        }

        var request = new RequestEntity(
                UUID.randomUUID(),
                imageId,
                null,
                username,
                StatusResponse.WIP);

        imageWipProducer.sendMessage(new ImageWipMessage(
                imageId, request.getId(), filterTypes));

        requestRepository.save(request);
        return request;
    }

    public void closeRequest(final UUID requestId, final UUID modifiedImageId) throws FileReadException, IOException {
        var request = requestRepository.findById(requestId).orElseThrow(
                () -> new EntityNotFoundException("No request with id " + requestId));
        request.setModifiedImageId(modifiedImageId);
        request.setStatus(StatusResponse.DONE);

        var originalImageMeta = imageMetaRepository.findById(request.getOriginalImageId()).orElseThrow(
                () -> new EntityNotFoundException("No original image with id " + request.getOriginalImageId()));
        imageMetaRepository.save(new ImageMetaEntity(
                modifiedImageId,
                originalImageMeta.getOriginalName(),
                fileStorageService.get(modifiedImageId).available(),
                originalImageMeta.getAuthor()));

        requestRepository.save(request);
    }

}

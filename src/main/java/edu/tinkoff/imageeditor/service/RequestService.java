package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.entity.FilterType;
import edu.tinkoff.imageeditor.entity.RequestEntity;
import edu.tinkoff.imageeditor.entity.StatusResponse;
import edu.tinkoff.imageeditor.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageeditor.kafka.producer.KafkaImageWipProducer;
import edu.tinkoff.imageeditor.repository.ImageMetaRepository;
import edu.tinkoff.imageeditor.repository.RequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ImageMetaRepository imageMetaRepository;
    private final KafkaImageWipProducer imageWipProducer;

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

    public void closeRequest(final UUID requestId, final UUID modifiedImageId) {
        var request = requestRepository.getReferenceById(requestId);
        request.setModifiedImageId(modifiedImageId);
        request.setStatus(StatusResponse.DONE);

        requestRepository.save(request);
    }

}

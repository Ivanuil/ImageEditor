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
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ImageMetaRepository imageMetaRepository;
    private final PreferencesService preferencesService;
    private final KafkaImageWipProducer imageWipProducer;
    private final MinioFileStorageService fileStorageService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private RateLimiter rateLimiter;

    @PostConstruct
    public void init() {
        rateLimiter = rateLimiterRegistry.rateLimiter("Imagga-request-ratelimiter");
    }

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
    public RequestEntity createRequest(final UUID imageId, final String username, final FilterType[] filterTypes)
            throws Exception {
        var imageOpt = imageMetaRepository.findById(imageId);
        if (imageOpt.isEmpty() || !imageOpt.get().getAuthor().getUsername().equals(username)) {
            throw new EntityNotFoundException("No image with id " + imageId);
        }

        Callable<RequestEntity> createRequestJob = () -> {
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
        };

        // Run job through ratelimiter if request contains OBJECT_RECOGNITION filter,
        // or simply execute callable otherwise
        if (Arrays.asList(filterTypes).contains(FilterType.OBJECT_RECOGNITION)) {
            if (preferencesService.getRemainingImaggaRequests() <= 0) {
                throw new RuntimeException("No remaining imagga requests");
            }
            preferencesService.decrementRemainingImaggaRequests();
            return rateLimiter.executeCallable(createRequestJob);
        } else {
            return createRequestJob.call();
        }

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
                (int) fileStorageService.getSize(modifiedImageId.toString()),
                originalImageMeta.getAuthor()));

        requestRepository.save(request);
    }

}

package edu.tinkoff.imageprocessor.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageprocessor.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageprocessor.repository.exception.FileReadException;
import edu.tinkoff.imageprocessor.repository.exception.FileWriteException;
import edu.tinkoff.imageprocessor.service.FilterProcessingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.RejectedExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class KafkaImageWipListener {

    @Value("${concurrency.min-thread-pool-size}")
    private int minThreadPoolSize;

    @Value("${concurrency.max-thread-pool-size}")
    private int maxThreadPoolSize;

    private final FilterProcessingService filterProcessingService;
    private final ObjectMapper mapper;
    private ExecutorService executorService = null;

    @PostConstruct
    public void start() {
        executorService = new ThreadPoolExecutor(
                minThreadPoolSize, maxThreadPoolSize, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    @PreDestroy
    public void stop() {
        executorService.shutdown();
    }

    @KafkaListener(topics = {"${spring.kafka.topic-name.images-wip}"}, autoStartup = "true")
    public void listen(final ConsumerRecord<?, ?> cr,
                       final Acknowledgment ack) {
        ImageWipMessage message;
        try {  // Try to parse message
            message = mapper.readValue(cr.value().toString(), ImageWipMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Unable to map String->ImageWipMessage", e);
            return;
        }

        //  Check if image is already processed
        if (filterProcessingService.isAlreadyProcessed(message.getRequestId(), message.getImageId())) {
            log.warn("Consumer picked up message " + message + ", but it is already processed");
            return;
        }

        if (filterProcessingService.isFilterAvailable(message.getFilters()[0])) {
            log.info("Accepted request for processing " + message);

            try {
                executorService.submit(() -> {
                    try {
                        filterProcessingService.applyFilter(message.getRequestId(),
                                message.getImageId(),
                                message.getFilters());
                        ack.acknowledge();  // Commit offset
                    } catch (FileReadException | IOException | FileWriteException e) {
                        log.error("Unable to process filter that already has bean acknowledged", e);
                    }
                });
            } catch (RejectedExecutionException e) {
                log.warn("ExecutorService overwhelmed, rejecting message", e);
            }
        }
    }

}


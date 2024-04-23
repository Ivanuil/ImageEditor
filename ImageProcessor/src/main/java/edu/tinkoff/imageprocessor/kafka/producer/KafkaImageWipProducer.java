package edu.tinkoff.imageprocessor.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageprocessor.kafka.messages.ImageWipMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class KafkaImageWipProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    @Value("${spring.kafka.topic-name.images-wip}")
    private String topicName;

    public void sendMessage(final ImageWipMessage message) {
        try {
            kafkaTemplate.send(topicName, mapper.writeValueAsString(message)).get(1000L, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            log.error("Unable to map ImageWipMessage->String", e);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            log.error("Writing to Kafka timeout");
//            throw new KafkaException("Writing timeout");
            // todo: fix
        }
    }

}

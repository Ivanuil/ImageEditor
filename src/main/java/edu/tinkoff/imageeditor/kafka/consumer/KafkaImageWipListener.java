package edu.tinkoff.imageeditor.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageeditor.kafka.messages.ImageWipMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// todo: remove this temporary class
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class KafkaImageWipListener {

    private final ObjectMapper mapper;

    @KafkaListener(topics = {"${spring.kafka.topic-name.images-wip}"}, autoStartup = "true")
    public void listen(final ConsumerRecord<?, ?> cr,
                       final Acknowledgment ack) {
        try {
            var message = mapper.readValue(cr.value().toString(), ImageWipMessage.class);
            log.info("Received message in topic {}: {}", cr.topic(), message);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Unable to map String->ImageWipMessage", e);
        }
    }

}


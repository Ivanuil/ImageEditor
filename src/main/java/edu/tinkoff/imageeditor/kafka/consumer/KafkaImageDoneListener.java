package edu.tinkoff.imageeditor.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageeditor.kafka.messages.ImageDoneMessage;
import edu.tinkoff.imageeditor.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class KafkaImageDoneListener {

    private final RequestService requestService;
    private final ObjectMapper mapper;

    @Value("${spring.kafka.topic-name.images-done}")
    private String topicName;

    @KafkaListener(topics = {"${spring.kafka.topic-name.images-done}"}, autoStartup = "true")
    public void listen(ConsumerRecord<?, ?> cr,
                       Acknowledgment ack) {
        try {
            var message = mapper.readValue(cr.value().toString(), ImageDoneMessage.class);
            requestService.closeRequest(message.getRequestId(), message.getImageId());
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Unable to map String->ImageDoneMessage", e);
        }
    }

}

package edu.tinkoff.imageeditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tinkoff.imageeditor.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageeditor.kafka.producer.KafkaImageWipProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "false")
public class KafkaImageWipProducerStub extends KafkaImageWipProducer {

    public KafkaImageWipProducerStub() {
        super(null, null);
    }

    @Override
    public void sendMessage(ImageWipMessage message) {
    }

}

package edu.tinkoff.imageprocessor.config;

import edu.tinkoff.imageprocessor.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageprocessor.kafka.producer.KafkaImageWipProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

package edu.tinkoff.imageeditorapi;

import edu.tinkoff.imageeditorapi.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageeditorapi.kafka.producer.KafkaImageWipProducer;
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

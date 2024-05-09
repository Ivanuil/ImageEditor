package edu.tinkoff.imageprocessor.config;

import edu.tinkoff.imageprocessor.kafka.messages.ImageDoneMessage;
import edu.tinkoff.imageprocessor.kafka.producer.KafkaImageDoneProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "false")
public class KafkaImageDoneProducerStub extends KafkaImageDoneProducer {

    public KafkaImageDoneProducerStub() {
        super(null, null);
    }

    @Override
    public void sendMessage(ImageDoneMessage message) {
    }

}

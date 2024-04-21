package edu.tinkoff.imageeditor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaAdmin;

@SpringBootApplication
@Slf4j
public class ImageEditorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ImageEditorApplication.class, args);
    }

}


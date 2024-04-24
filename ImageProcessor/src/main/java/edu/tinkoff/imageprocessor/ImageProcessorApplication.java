package edu.tinkoff.imageprocessor;

import edu.tinkoff.imageprocessor.config.FiltersProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FiltersProperty.class)
public class ImageProcessorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ImageProcessorApplication.class, args);
    }

}

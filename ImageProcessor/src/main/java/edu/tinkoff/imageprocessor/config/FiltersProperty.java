package edu.tinkoff.imageprocessor.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "filters")
@Setter
public class FiltersProperty {

    private boolean rotate = false;
    private boolean slow = false;

}

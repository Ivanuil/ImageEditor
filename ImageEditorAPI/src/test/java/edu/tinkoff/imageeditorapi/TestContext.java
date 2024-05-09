package edu.tinkoff.imageeditorapi;

import edu.tinkoff.imageeditorapi.config.MinIOTestConfig;
import edu.tinkoff.imageeditorapi.config.PostgreSQLTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    value = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@ContextConfiguration(initializers = {
        MinIOTestConfig.Initializer.class,
        PostgreSQLTestConfig.Initializer.class
})
public class TestContext {
}

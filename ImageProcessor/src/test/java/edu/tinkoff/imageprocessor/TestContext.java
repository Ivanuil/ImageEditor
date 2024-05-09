package edu.tinkoff.imageprocessor;

import edu.tinkoff.imageprocessor.config.MinIOTestConfig;
import edu.tinkoff.imageprocessor.config.PostgreSQLTestConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(initializers = {
        MinIOTestConfig.Initializer.class,
        PostgreSQLTestConfig.Initializer.class
})
@SpringBootTest(
        value = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
public class TestContext {
}

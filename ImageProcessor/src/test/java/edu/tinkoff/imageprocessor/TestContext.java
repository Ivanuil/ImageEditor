package edu.tinkoff.imageprocessor;

import edu.tinkoff.imageprocessor.config.MinIOTestConfig;
import edu.tinkoff.imageprocessor.config.PostgreSQLTestConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = {
        MinIOTestConfig.Initializer.class,
        PostgreSQLTestConfig.Initializer.class
})
public class TestContext {
}

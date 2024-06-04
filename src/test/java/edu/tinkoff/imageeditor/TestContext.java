package edu.tinkoff.imageeditor;

import edu.tinkoff.imageeditor.config.MinIOTestConfig;
import edu.tinkoff.imageeditor.config.PostgreSQLTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = {
        MinIOTestConfig.Initializer.class,
        PostgreSQLTestConfig.Initializer.class
})
public class TestContext {
}

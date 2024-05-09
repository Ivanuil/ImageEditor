package edu.tinkoff.imageprocessor.service.processors;

import edu.tinkoff.imageprocessor.entity.FilterType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "filters.slow", havingValue = "true")
public class SlowFilterProcessor implements ImageFilterProcessor {

    @Override
    public FilterType getFilterType() {
        return FilterType.SLOW_FILTER;
    }

    @Override
    public InputStream process(final InputStream stream) throws IOException {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return stream;
    }

}

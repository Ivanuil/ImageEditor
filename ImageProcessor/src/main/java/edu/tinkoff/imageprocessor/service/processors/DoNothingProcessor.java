package edu.tinkoff.imageprocessor.service.processors;

import edu.tinkoff.imageprocessor.entity.FilterType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@ConditionalOnProperty(name = "filters.do-nothing", havingValue = "true")
public class DoNothingProcessor implements ImageFilterProcessor {

    @Override
    public FilterType getFilterType() {
        return FilterType.DO_NOTHING;
    }

    @Override
    public InputStream process(final InputStream stream) throws IOException {
        return stream;
    }

}

package edu.tinkoff.imageprocessor.service.processors;

import edu.tinkoff.imageprocessor.entity.FilterType;

import java.io.IOException;
import java.io.InputStream;

public interface ImageFilterProcessor {

    FilterType getFilterType();

    InputStream process(InputStream stream) throws IOException;

}

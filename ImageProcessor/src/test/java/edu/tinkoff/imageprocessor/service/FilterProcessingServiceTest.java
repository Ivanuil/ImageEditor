package edu.tinkoff.imageprocessor.service;

import edu.tinkoff.imageprocessor.TestContext;
import edu.tinkoff.imageprocessor.entity.FilterType;
import edu.tinkoff.imageprocessor.repository.MinioFileStorage;
import edu.tinkoff.imageprocessor.repository.ProcessedRequestsRepository;
import edu.tinkoff.imageprocessor.repository.exception.FileReadException;
import edu.tinkoff.imageprocessor.repository.exception.FileWriteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterProcessingServiceTest extends TestContext {

    @Autowired
    private FilterProcessingService filterProcessingService;
    @Autowired
    private ProcessedRequestsRepository processedRequestsRepository;
    @Autowired
    private MinioFileStorage fileStorage;

    private File testImageFile = new File("image.png");

    @BeforeEach
    public void registerUser() throws IOException {
        testImageFile.createNewFile();
        testImageFile.deleteOnExit();
        var writer = new BufferedWriter(new FileWriter(testImageFile));
        writer.write("SOME CONTENT");
        writer.close();
    }

    @AfterEach
    public void clear() {
        processedRequestsRepository.deleteAll();
    }

    @Test
    public void isFilterAvailableTest() {
        assertTrue(filterProcessingService.isFilterAvailable(FilterType.DO_NOTHING));
        assertTrue(filterProcessingService.isFilterAvailable(FilterType.ROTATE_90_DEGREES_CLOCKWISE));
        assertFalse(filterProcessingService.isFilterAvailable(FilterType.SLOW_FILTER));
    }

    @Test
    public void applyFilterTest() throws IOException, FileWriteException, FileReadException {
        // given
        var requestId = UUID.randomUUID();
        var imageId = UUID.randomUUID();
        fileStorage.saveObject(imageId.toString(), new FileInputStream(testImageFile), false);

        // when
        filterProcessingService.applyFilter(requestId, imageId, new FilterType[] {FilterType.DO_NOTHING});

        // then
        assertTrue(filterProcessingService.isAlreadyProcessed(requestId, imageId));
    }

}

package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.TestContext;
import edu.tinkoff.imageeditor.repository.ImageMetaRepository;
import edu.tinkoff.imageeditor.repository.exception.FileWriteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageServiceTest extends TestContext {

    @Autowired
    private ImageMetaRepository metaRepository;
    @Autowired
    private ImageStorageService storageService;

    @BeforeEach
    @AfterEach
    public void clean() throws FileWriteException {
        for (var image : metaRepository.findAll())
            storageService.delete(String.valueOf(image.getId()));
        metaRepository.deleteAll();
    }

    @Test
    public void saveImage() {

    }

}

package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.TestContext;
import edu.tinkoff.imageeditorapi.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditorapi.repository.ImageMetaRepository;
import edu.tinkoff.imageeditorapi.repository.TokenRepository;
import edu.tinkoff.imageeditorapi.repository.UserRepository;
import edu.tinkoff.imageeditorapi.repository.exception.FileReadException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageServiceTest extends TestContext {

    @Autowired ImageService imageService;
    @Autowired AuthService authService;
    @Autowired TokenRepository tokenRepository;
    @Autowired UserRepository userRepository;
    @Autowired ImageMetaRepository metaRepository;

    private static final String FILE_ORIG_NAME = "file.png";
    private static final String FILE_TEXT = "Hello, World!";

    private static final String USERNAME = "username";
    private static final String PASSWORD = "pass123!";

    @BeforeEach
    public void registerUser() {
        authService.register(new RegisterRequestDto(USERNAME, PASSWORD));
    }

    @AfterEach
    public void cleanup() {
        for (var image : metaRepository.findAll())
            imageService.deleteImage(image.getId());
        metaRepository.deleteAll();

        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void storeFile() throws FileReadException, IOException {
        MockMultipartFile file = new MockMultipartFile(
                FILE_ORIG_NAME, FILE_ORIG_NAME,
                MediaType.TEXT_PLAIN_VALUE,
                FILE_TEXT.getBytes());

        var id = imageService.uploadImage(file, USERNAME);

        var stream = imageService.downloadImage(id);
        assertArrayEquals(FILE_TEXT.getBytes(), stream.getInputStream().readAllBytes());

        var meta = imageService.getImageMeta(id);
        assertEquals(USERNAME, meta.getAuthor().getUsername());
        assertEquals(id, meta.getId());

        var imagesByUser = imageService.getImages(USERNAME);
        assertEquals(1, imagesByUser.size());
        assertEquals(id, imagesByUser.get(0).getId());
    }

    @Test
    public void deleteFile() {
        MockMultipartFile file = new MockMultipartFile(
                FILE_ORIG_NAME, FILE_ORIG_NAME,
                MediaType.TEXT_PLAIN_VALUE,
                FILE_TEXT.getBytes());
        var id = imageService.uploadImage(file, USERNAME);

        imageService.deleteImage(id);

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> imageService.getImageMeta(id));
    }

    @Test
    public void downloadNonExisting() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> imageService.downloadImage(UUID.randomUUID()));
    }

    @Test
    public void deleteNonExisting() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> imageService.deleteImage(UUID.randomUUID()));
    }

}

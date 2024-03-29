package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.TestContext;
import edu.tinkoff.imageeditor.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditor.repository.ImageMetaRepository;
import edu.tinkoff.imageeditor.repository.TokenRepository;
import edu.tinkoff.imageeditor.repository.UserRepository;
import edu.tinkoff.imageeditor.repository.exception.FileReadException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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
    public void storeFileInvalidType() {
        MockMultipartFile file = new MockMultipartFile(
                "file.txt", "file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                FILE_TEXT.getBytes());

        Assertions.assertThrows(ConstraintViolationException.class,
                () -> imageService.uploadImage(file, USERNAME));
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

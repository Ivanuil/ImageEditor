package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.TestContext;
import edu.tinkoff.imageeditorapi.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditorapi.entity.FilterType;
import edu.tinkoff.imageeditorapi.entity.ImageMetaEntity;
import edu.tinkoff.imageeditorapi.entity.StatusResponse;
import edu.tinkoff.imageeditorapi.entity.UserEntity;
import edu.tinkoff.imageeditorapi.kafka.producer.KafkaImageWipProducer;
import edu.tinkoff.imageeditorapi.repository.ImageMetaRepository;
import edu.tinkoff.imageeditorapi.repository.RequestRepository;
import edu.tinkoff.imageeditorapi.repository.TokenRepository;
import edu.tinkoff.imageeditorapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RequestServiceTest extends TestContext {

    @Autowired
    @InjectMocks
    private RequestService requestService;
    @Autowired
    private AuthService authService;

    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private ImageMetaRepository imageMetaRepository;
    @Autowired
    private UserRepository userRepository;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "pass123!";
    private UserEntity user;

    @BeforeEach
    public void registerUser() {
        authService.register(new RegisterRequestDto(USERNAME, PASSWORD));
        user = userRepository.getReferenceById(USERNAME);
    }

    @AfterEach
    public void clear() {
        requestRepository.deleteAll();
        imageMetaRepository.deleteAll();

        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Mock
    private KafkaImageWipProducer imageWipProducer;

    @Test
    @ExtendWith(MockitoExtension.class)
    public void createRequestTest() {
        // given
        var imageId = UUID.randomUUID();
        imageMetaRepository.save(new ImageMetaEntity(
                imageId,
                "image.png",
                1234,
                user));

        // when
        var requestId = requestService.createRequest(imageId, USERNAME,
                new FilterType[] {FilterType.REVERS_COLORS}).getId();

        // then
        var request = requestService.getRequest(requestId, imageId);
        assertEquals(imageId, request.getOriginalImageId());
        assertNull(request.getModifiedImageId());
        assertEquals(StatusResponse.WIP, request.getStatus());
    }

    @Test
    @ExtendWith(MockitoExtension.class)
    public void createRequestNoImageTest() {
        // when
        assertThrows(EntityNotFoundException.class, () -> requestService.createRequest(UUID.randomUUID(), USERNAME,
                new FilterType[]{FilterType.REVERS_COLORS}).getId());
    }

    @Test
    @ExtendWith(MockitoExtension.class)
    public void getRequestNotFoundTest() {
        assertThrows(EntityNotFoundException.class, () ->
                requestService.getRequest(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    @ExtendWith(MockitoExtension.class)
    public void getRequestWrongImageIdTest() {
        // given
        var imageId = UUID.randomUUID();
        imageMetaRepository.save(new ImageMetaEntity(
                imageId,
                "image.png",
                1234,
                user));
        var requestId = requestService.createRequest(imageId, USERNAME,
                new FilterType[] {FilterType.REVERS_COLORS}).getId();

        // then
        Assertions.assertThrows(EntityNotFoundException.class, () ->
                requestService.getRequest(requestId, UUID.randomUUID()));
    }

    @Test
    @ExtendWith(MockitoExtension.class)
    public void closeRequestTest() {
        // given
        var imageId = UUID.randomUUID();
        imageMetaRepository.save(new ImageMetaEntity(
                imageId,
                "image.png",
                1234,
                user));
        var requestId = requestService.createRequest(imageId, USERNAME,
                new FilterType[] {FilterType.REVERS_COLORS}).getId();

        // when
        var newImageId = UUID.randomUUID();
        requestService.closeRequest(requestId, newImageId);

        // then
        var request = requestService.getRequest(requestId, imageId);
        assertEquals(imageId, request.getOriginalImageId());
        assertEquals(newImageId, request.getModifiedImageId());
        assertEquals(StatusResponse.DONE, request.getStatus());
    }

    @Test
    @ExtendWith(MockitoExtension.class)
    public void closeNonExistentRequestTest() {
        assertThrows(EntityNotFoundException.class, () ->
                requestService.closeRequest(UUID.randomUUID(), UUID.randomUUID()));
    }

}

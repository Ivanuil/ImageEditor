package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.TestContext;
import edu.tinkoff.imageeditor.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditor.entity.FilterType;
import edu.tinkoff.imageeditor.entity.ImageMetaEntity;
import edu.tinkoff.imageeditor.entity.StatusResponse;
import edu.tinkoff.imageeditor.entity.UserEntity;
import edu.tinkoff.imageeditor.kafka.messages.ImageWipMessage;
import edu.tinkoff.imageeditor.kafka.producer.KafkaImageWipProducer;
import edu.tinkoff.imageeditor.repository.ImageMetaRepository;
import edu.tinkoff.imageeditor.repository.RequestRepository;
import edu.tinkoff.imageeditor.repository.TokenRepository;
import edu.tinkoff.imageeditor.repository.UserRepository;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

}
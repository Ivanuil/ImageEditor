package edu.tinkoff.imageeditor.web.controller;

import edu.tinkoff.imageeditor.TestContext;
import edu.tinkoff.imageeditor.dto.UiSuccessContainer;
import edu.tinkoff.imageeditor.dto.auth.AuthUserDto;
import edu.tinkoff.imageeditor.dto.auth.LoginRequestDto;
import edu.tinkoff.imageeditor.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditor.repository.TokenRepository;
import edu.tinkoff.imageeditor.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static edu.tinkoff.imageeditor.web.security.SecurityConstants.JWT_COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestAuthController extends TestContext {

    private final RestClient restClient = RestClient.create();

    @Autowired private UserRepository userRepository;
    @Autowired private TokenRepository tokenRepository;

    private static final String REGISTER_URI = "http://localhost:8080/api/v1/auth/register";
    private static final String LOGIN_URI = "http://localhost:8080/api/v1/auth/login";
    private static final String USER_URI = "http://localhost:8080/api/v1/auth/user";

    private static final String USERNAME = "user123";
    private static final String PASSWORD = "qwerty";

    @BeforeEach
    @AfterEach
    public void clean() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void registerNewUser() {
        // Register
        var responseEntity = restClient.post()
                .uri(REGISTER_URI)
                .body(new RegisterRequestDto(USERNAME, PASSWORD))
                .retrieve()
                .toBodilessEntity();
        assertEquals(200, responseEntity.getStatusCode().value());

        // Assert token is written to Cookie
        String token = tokenRepository.findByUser_Username(USERNAME).get(0).getToken();
        String setCookie = responseEntity.getHeaders().get("Set-Cookie")
                .stream().filter(s -> s.startsWith(JWT_COOKIE_NAME)).findFirst().get();
        Assertions.assertTrue(setCookie.contains(token));
    }

//    @Test
//    public void registerDuplicate() {
//        // Register first user
//        restClient.post()
//                .uri(REGISTER_URI)
//                .body(new RegisterRequestDto(USERNAME, PASSWORD))
//                .retrieve()
//                .toBodilessEntity();
//
//        // Register new user with already taken username
//        var responseEntity = restClient.post()
//                .uri(REGISTER_URI)
//                .body(new RegisterRequestDto(USERNAME, PASSWORD + "new"))
//                .retrieve()
//                .toEntity(UiSuccessContainer.class);
//        assertEquals(403, responseEntity.getStatusCode().value());
//    }

    @Test
    public void loginUser() {
        // Register
        restClient.post()
                .uri(REGISTER_URI)
                .body(new RegisterRequestDto(USERNAME, PASSWORD))
                .retrieve();
        tokenRepository.deleteAll();

        // Login
        var responseEntity = restClient.post()
                .uri(LOGIN_URI)
                .body(new LoginRequestDto(USERNAME, PASSWORD))
                .retrieve()
                .toEntity(AuthUserDto.class);
        assertEquals(200, responseEntity.getStatusCode().value());

        // Check if authorised
        String token = tokenRepository.findByUser_Username(USERNAME).get(0).getToken();
        var authUserDto = restClient.get()
                .uri(USER_URI)
                .header("Cookie", JWT_COOKIE_NAME + "=" + token)
                .retrieve()
                .body(AuthUserDto.class);
        assertEquals(USERNAME, authUserDto.getUsername());
    }

//    @Test
//    public void loginUnregistered() {
//        // Login
//        var responseEntity = restClient.post()
//                .uri(LOGIN_URI)
//                .body(new LoginRequestDto(USERNAME, PASSWORD))
//                .retrieve()
//                .toEntity(UiSuccessContainer.class);
//        assertEquals(403, responseEntity.getStatusCode().value());
//    }

}

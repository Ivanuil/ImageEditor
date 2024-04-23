package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.TestContext;
import edu.tinkoff.imageeditorapi.dto.auth.LoginRequestDto;
import edu.tinkoff.imageeditorapi.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditorapi.repository.TokenRepository;
import edu.tinkoff.imageeditorapi.repository.UserRepository;
import edu.tinkoff.imageeditorapi.service.exception.AuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthServiceTest extends TestContext {

    @Autowired AuthService authService;

    @Autowired TokenRepository tokenRepository;
    @Autowired UserRepository userRepository;

    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    @BeforeEach
    @AfterEach
    public void clean() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void registerDuplicate() {
        // given
        authService.register(new RegisterRequestDto(USERNAME, PASSWORD));

        // when
        assertThrows(AuthenticationException.class,
                () -> authService.register(new RegisterRequestDto(USERNAME, "anotherpass")));
    }

    @Test
    public void loginUnregistered() {
        assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequestDto(USERNAME, PASSWORD)));
    }

    @Test
    public void getRoleUnregistered() {
        assertThrows(AuthenticationException.class,
                () -> authService.getUserRole(USERNAME));
    }

}

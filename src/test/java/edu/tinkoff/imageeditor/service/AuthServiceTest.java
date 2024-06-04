package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.TestContext;
import edu.tinkoff.imageeditor.dto.auth.LoginRequestDto;
import edu.tinkoff.imageeditor.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditor.repository.TokenRepository;
import edu.tinkoff.imageeditor.repository.UserRepository;
import edu.tinkoff.imageeditor.service.exception.AuthenticationException;
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

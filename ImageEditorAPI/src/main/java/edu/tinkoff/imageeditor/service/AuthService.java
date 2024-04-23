package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.dto.auth.AuthUserDto;
import edu.tinkoff.imageeditor.dto.auth.LoginRequestDto;
import edu.tinkoff.imageeditor.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditor.entity.Role;
import edu.tinkoff.imageeditor.entity.UserEntity;
import edu.tinkoff.imageeditor.repository.UserRepository;
import edu.tinkoff.imageeditor.service.exception.AuthenticationException;
import edu.tinkoff.imageeditor.web.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final TokenService tokenService;

    /**
     * Creates user in the database with given username and password. It generates a token based on
     * username and saves that token in the database.
     * @param registerRequest object that contains username and password
     * @return user dto with roles and generated token
     */
    public AuthUserDto register(final RegisterRequestDto registerRequest) {
        if (userRepository.existsById(registerRequest.getUsername())) {
            throw new AuthenticationException("User with that username already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        String generatedToken = jwtService.generateFromUser(user);
        tokenService.saveNewToken(generatedToken, user);
        return new AuthUserDto(generatedToken, user.getUsername(),
                user.getRole().getRoles().stream().map(Role::toString).collect(Collectors.toSet()));
    }

    /**
     * Authenticates the user with provided username and password. It creates new active token and associates it
     * with the user. All existing user token are deactivated.
     * @param loginRequest object that contains username and password
     * @return user dto with roles and generated token
     */
    @Transactional
    public AuthUserDto login(final LoginRequestDto loginRequest) {
        if (userRepository.findById(loginRequest.getUsername()).isEmpty()) {
            throw new AuthenticationException("No user with username: " + loginRequest.getUsername());
        }

        authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        UserEntity user = userRepository.findById(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user cannot be null"));

        tokenService.deactivateUserTokens(user.getUsername());
        String generatedToken = jwtService.generateFromUser(user);
        tokenService.saveNewToken(generatedToken, user);
        return new AuthUserDto(generatedToken, user.getUsername(),
                user.getRole().getRoles().stream().map(Role::toString).collect(Collectors.toSet()));
    }

    public Role getUserRole(final String username) {
        return userRepository.findById(username).orElseThrow(() ->
                new AuthenticationException("No user with username: " + username)).getRole();
    }

}

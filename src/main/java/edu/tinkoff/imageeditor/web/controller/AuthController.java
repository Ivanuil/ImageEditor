package edu.tinkoff.imageeditor.web.controller;

import edu.tinkoff.imageeditor.dto.auth.AuthUserDto;
import edu.tinkoff.imageeditor.dto.auth.LoginRequestDto;
import edu.tinkoff.imageeditor.dto.auth.RegisterRequestDto;
import edu.tinkoff.imageeditor.entity.Role;
import edu.tinkoff.imageeditor.web.security.SecurityConstants;
import edu.tinkoff.imageeditor.web.security.UserDetailsImpl;
import edu.tinkoff.imageeditor.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Контроллер для регистрации/авторизации пользователя")
public class AuthController {

    private final AuthService authService;

    // TODO: Расписать @ApiResponses
    @GetMapping("/user")
    @Operation(summary = "Возвращает информацию о авторизованном пользователе")
    public ResponseEntity<AuthUserDto> user(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        AuthUserDto user = new AuthUserDto();
        user.setUsername(userDetails.getUsername());
        user.setRoles(authService.getUserRole(userDetails.getUsername())
                .getRoles().stream().map(Role::toString).collect(Collectors.toSet()));
        return ResponseEntity.ok(user);
    }

    // TODO: Расписать @ApiResponses
    @PostMapping("/register")
    @Operation(summary = "Регистрирует нового пользователя")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDto requestDto,
                                      HttpServletResponse response) {
        AuthUserDto authUser = authService.register(requestDto);
        addTokenCookieToResponse(response, authUser.getToken());
        return ResponseEntity.ok(authUser);
    }

    // TODO: Расписать @ApiResponses
    @PostMapping("/login")
    @Operation(summary = "Авторизовывает зарегистрированного пользователя")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto,
                                   HttpServletResponse response) {
        AuthUserDto authUser = authService.login(requestDto);
        addTokenCookieToResponse(response, authUser.getToken());
        return ResponseEntity.ok(authUser);
    }

    private void addTokenCookieToResponse(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(SecurityConstants.JWT_COOKIE_NAME, token);
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}

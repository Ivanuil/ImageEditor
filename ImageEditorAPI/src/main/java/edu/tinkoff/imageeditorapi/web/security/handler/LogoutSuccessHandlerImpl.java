package edu.tinkoff.imageeditorapi.web.security.handler;

import edu.tinkoff.imageeditorapi.entity.TokenEntity;
import edu.tinkoff.imageeditorapi.service.TokenService;
import edu.tinkoff.imageeditorapi.web.security.SecurityConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final TokenService tokenService;

    @Override
    public void onLogoutSuccess(final HttpServletRequest request,
                                final HttpServletResponse response, final Authentication authentication) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> tokenCookieOpt = Optional.empty();
        if (Objects.nonNull(cookies)) {
            tokenCookieOpt = Arrays.stream(cookies)
                    .filter(c -> SecurityConstants.JWT_COOKIE_NAME.equals(c.getName()))
                    .findFirst();
        }

        if (tokenCookieOpt.isPresent()) {

            String tokenValue = tokenCookieOpt.get().getValue();
            Optional<TokenEntity> tokenOptional = tokenService.findByTokenValue(tokenValue);
            tokenOptional.ifPresent(tokenService::makeInactive);

            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}

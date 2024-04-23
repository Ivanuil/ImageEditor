package edu.tinkoff.imageeditorapi.web.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Objects;

public class JwtAuthentication extends AbstractAuthenticationToken {

    private final String token;
    private String username;
    private UserDetails userDetails;

    public JwtAuthentication(final String token) {
        super(null);
        this.token = token;
    }

    public JwtAuthentication(final String token, final String username, final UserDetails userDetails) {
        super(userDetails.getAuthorities());
        this.token = token;
        this.username = username;
        this.userDetails = userDetails;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return Objects.nonNull(userDetails) ? userDetails : username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setUserDetails(final UserDetails userDetails) {
        this.userDetails = userDetails;
    }
}

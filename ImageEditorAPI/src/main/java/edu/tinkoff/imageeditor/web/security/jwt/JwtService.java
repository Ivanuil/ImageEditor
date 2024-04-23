package edu.tinkoff.imageeditor.web.security.jwt;

import edu.tinkoff.imageeditor.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt.expiration-time}")
    private long jwtExpiration;

    public String generateFromUser(final UserEntity user) {
        return buildToken(new HashMap<>(), user, jwtExpiration);
    }

    private String buildToken(final Map<String, Object> extraClaims, final UserEntity user, final long expiration) {
        Instant currentDate = Instant.now();
        Instant expirationDate = currentDate.plus(expiration, ChronoUnit.MILLIS);

        return Jwts.builder()
                .claims().add(extraClaims)
                .and()
                .subject(user.getUsername())
                .issuedAt(Date.from(currentDate))
                .expiration(Date.from(expirationDate))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractSubject(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(final String token, final Function<Claims, T> claimResolver) {
        Claims allClaims = extractAllClaims(token);
        return claimResolver.apply(allClaims);
    }

    private Claims extractAllClaims(final String token) {
        return Jwts.parser().verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

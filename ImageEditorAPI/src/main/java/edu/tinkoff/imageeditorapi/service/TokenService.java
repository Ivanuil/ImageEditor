package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.entity.TokenEntity;
import edu.tinkoff.imageeditorapi.entity.UserEntity;
import edu.tinkoff.imageeditorapi.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(final TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public TokenEntity saveNewToken(final String tokenValue, final UserEntity user) {
        TokenEntity token = new TokenEntity();
        token.setToken(tokenValue);
        token.setActive(true);
        token.setUser(user);
        return tokenRepository.save(token);
    }

    public Optional<TokenEntity> findByTokenValue(final String token) {
        return tokenRepository.findByToken(token);
    }

    public void makeInactive(final TokenEntity token) {
        token.setActive(false);
        tokenRepository.save(token);
    }

    public void deactivateUserTokens(final String username) {
        List<TokenEntity> activeTokens = tokenRepository.findByUser_UsernameAndIsActive(username, true);
        activeTokens.forEach(token -> token.setActive(false));
        tokenRepository.saveAll(activeTokens);
    }
}

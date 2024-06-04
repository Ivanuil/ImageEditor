package edu.tinkoff.imageeditor.service;

import edu.tinkoff.imageeditor.entity.TokenEntity;
import edu.tinkoff.imageeditor.entity.UserEntity;
import edu.tinkoff.imageeditor.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public TokenEntity saveNewToken(String tokenValue, UserEntity user) {
        TokenEntity token = new TokenEntity();
        token.setToken(tokenValue);
        token.setActive(true);
        token.setUser(user);
        return tokenRepository.save(token);
    }

    public Optional<TokenEntity> findByTokenValue(String token) {
        return tokenRepository.findByToken(token);
    }

    public void makeInactive(TokenEntity token) {
        token.setActive(false);
        tokenRepository.save(token);
    }

    public void deactivateUserTokens(String username) {
        List<TokenEntity> activeTokens = tokenRepository.findByUser_UsernameAndIsActive(username, true);
        activeTokens.forEach(token -> token.setActive(false));
        tokenRepository.saveAll(activeTokens);
    }
}

package com.example.shopapp.service.impl;

import com.example.shopapp.components.JwtTokenUtil;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.service.ITokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {

    private static final int MAX_TOKENS = 3;

    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    private final TokenRepository tokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Transactional
    @Override
    public Token addToken(User user, String token, boolean isMobileDevice) {
        List<Token> userTokens = tokenRepository.findByUser(user);
        int tokenCount = userTokens.size();
        log.info("User {} has {} token(s)", user.getId(), tokenCount);

        if (tokenCount >= MAX_TOKENS) {
            // Sort tokens by creation date
            userTokens.sort(Comparator.comparing(Token::getCreatedAt));

            // Find and delete a non-mobile token first, or fallback to the oldest token
            Token tokenToDelete = userTokens.stream()
                    .filter(t -> !t.isMobile())
                    .findFirst()
                    .orElse(userTokens.get(0));

            log.info("Deleting token id={} for user {}", tokenToDelete.getId(), user.getId());
            tokenRepository.delete(tokenToDelete);
        }

        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);

        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .revoked(false)
                .expired(false)
                .isMobile(isMobileDevice)
                .refreshToken(UUID.randomUUID().toString())
                .refreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken))
                .build();

        tokenRepository.save(newToken);
        log.info("Saved new token for user {}", user.getId());
        return newToken;
    }

    @Transactional
    @Override
    public Token refreshToken(String refreshToken, User user) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);

        if (existingToken == null) {
            throw new DataNotFoundException("Refresh token does not exist");
        }

        if (existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(existingToken);
            throw new RuntimeException("Refresh token is expired");
        }

        String token = jwtTokenUtil.generateToken(user);
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);

        existingToken.setToken(token);
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setRefreshToken(UUID.randomUUID().toString());
        existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));

        tokenRepository.save(existingToken); // <-- Don't forget to persist the changes

        return existingToken;
    }
}

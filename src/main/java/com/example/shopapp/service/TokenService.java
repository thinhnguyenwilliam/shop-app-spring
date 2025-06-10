package com.example.shopapp.service;

import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {

    private static final int MAX_TOKENS = 3;

    @Value("${jwt.expiration}")
    private int expiration;

    private final TokenRepository tokenRepository;

    @Transactional
    @Override
    public void addToken(User user, String token, boolean isMobileDevice) {
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
                .build();

        tokenRepository.save(newToken);
        log.info("Saved new token for user {}", user.getId());
    }
}

package com.example.shopapp.components;

import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final TokenRepository tokenRepository;

    @Value("${jwt.expiration}")
    private int expiration; // in seconds

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());
        claims.put("role", "ROLE_" + user.getRole().getName().toUpperCase());

        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract all claims from a token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract a specific claim using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        // Extract expiration date from token
        Date expirationDate = extractClaim(token, Claims::getExpiration);

        // Check if the expiration date is before now
        return expirationDate.before(new Date());
    }

    public String getPhoneNumberFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        String phoneNumber = getPhoneNumberFromToken(token);
        Token existingToken = tokenRepository.findByToken(token);

        if (existingToken == null ||
                Boolean.TRUE.equals(existingToken.getRevoked())
                || Boolean.FALSE.equals(existingToken.getUser().getIsActive())
        )
        {
            return false;
        }

        return phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

}

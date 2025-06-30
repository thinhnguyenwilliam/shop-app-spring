package com.example.shopapp.service.impl;

import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.service.IAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateAuthUrl(String loginType) {
        loginType = loginType.trim().toLowerCase();

        if ("google".equals(loginType)) {
            return new GoogleAuthorizationCodeRequestUrl(
                    googleClientId,
                    googleRedirectUri,
                    Arrays.asList("openid", "profile", "email")
            ).build();
        }

        throw new IllegalArgumentException("Unsupported login type: " + loginType);
    }

    @Override
    public UserLoginDTO authenticateSocialUser(String code, String loginType) throws IOException {
        Map<String, Object> userInfo = authenticateAndFetchProfile(code, loginType);

        if (userInfo == null || userInfo.isEmpty()) {
            throw new IllegalArgumentException("Could not authenticate user from provider");
        }

        String accountId = (String) userInfo.getOrDefault("sub", "");
        String email = (String) userInfo.getOrDefault("email", "");
        String name = (String) userInfo.getOrDefault("name", "");
        String picture = (String) userInfo.getOrDefault("picture", "");

        return UserLoginDTO.builder()
                .email(email)
                .fullName(name)
                .phoneNumber("") // Optional
                .password("")    // No password for social login
                .profileImage(picture)
                .googleAccountId(accountId)
                .roleId(2) // default role USER
                .build();
    }

    @Override
    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException {
        if (!"google".equalsIgnoreCase(loginType)) {
            log.warn("Unsupported login type: {}", loginType);
            return Collections.emptyMap();
        }
        log.info("Code: {}", code);
        log.info("Redirect URI: {}", googleRedirectUri);
        log.info("Client ID: {}", googleClientId);
        log.info("Client Secret: {}", googleClientSecret);


        // Step 1: Exchange authorization code for access token
        String accessToken = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                googleClientId,
                googleClientSecret,
                code,
                googleRedirectUri
        ).execute().getAccessToken();

        // Step 2: Fetch user info with access token
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setBearerAuth(accessToken);
            return execution.execute(request, body);
        });

        String response = restTemplate.getForObject(googleUserInfoUri, String.class);
        return objectMapper.readValue(response, new TypeReference<>() {});
    }
}

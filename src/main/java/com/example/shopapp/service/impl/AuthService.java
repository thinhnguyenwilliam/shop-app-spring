package com.example.shopapp.service.impl;

import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.service.IAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    @Value("${spring.security.oauth2.client.provider.facebook.authorization-uri}")
    private String facebookAuthUri;

    @Value("${spring.security.oauth2.client.provider.facebook.user-info-uri}")
    private String facebookUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.provider.facebook.token-uri}")
    private String facebookTokenUri;


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
        } else if ("facebook".equals(loginType)) {
            return UriComponentsBuilder
                    .fromUriString(facebookAuthUri)
                    .queryParam("client_id", facebookClientId)
                    .queryParam("redirect_uri", facebookRedirectUri)
                    .queryParam("scope", "email,public_profile")
                    .queryParam("response_type", "code")
                    .build()
                    .toUriString();
        }

        throw new IllegalArgumentException("Unsupported login type: " + loginType);
    }


    @Override
    public UserLoginDTO authenticateSocialUser(String code, String loginType) throws IOException {
        Map<String, Object> userInfo = authenticateAndFetchProfile(code, loginType);

        if (userInfo == null || userInfo.isEmpty()) {
            throw new IllegalArgumentException("Could not authenticate user from provider");
        }

        String accountId = "";
        String email = "";
        String name = "";
        String picture = "";

        loginType = loginType.trim().toLowerCase();

        if ("google".equals(loginType)) {
            accountId = (String) userInfo.getOrDefault("sub", "");
            email = (String) userInfo.getOrDefault("email", "");
            name = (String) userInfo.getOrDefault("name", "");
            picture = (String) userInfo.getOrDefault("picture", "");

            return UserLoginDTO.builder()
                    .email(email)
                    .fullName(name)
                    .phoneNumber("")
                    .password("")
                    .profileImage(picture)
                    .googleAccountId(accountId)
                    .roleId(2)
                    .build();

        } else if ("facebook".equals(loginType)) {
            accountId = (String) userInfo.getOrDefault("id", "");
            name = (String) userInfo.getOrDefault("name", "");
            email = (String) userInfo.getOrDefault("email", "");

            Object pictureObj = userInfo.get("picture");
            if (pictureObj instanceof Map<?, ?> pictureData) {
                Object dataObj = pictureData.get("data");
                if (dataObj instanceof Map<?, ?> dataMap) {
                    Object urlObj = dataMap.get("url");
                    if (urlObj instanceof String) {
                        picture = (String) urlObj;
                    }
                }
            }

            return UserLoginDTO.builder()
                    .email(email)
                    .fullName(name)
                    .phoneNumber("")
                    .password("")
                    .profileImage(picture)
                    .facebookAccountId(accountId)
                    .roleId(2)
                    .build();
        } else {
            throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }
    }



    @Override
    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        switch (loginType.toLowerCase()) {
            case "google":
                log.info("Code: {}", code);
                log.info("Redirect URI: {}", googleRedirectUri);
                log.info("Client ID: {}", googleClientId);

                // Step 1: Exchange code for access token
                String googleAccessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        googleClientId,
                        googleClientSecret,
                        code,
                        googleRedirectUri
                ).execute().getAccessToken();

                // Step 2: Use access token to fetch user info
                restTemplate.getInterceptors().add((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(googleAccessToken);
                    return execution.execute(request, body);
                });

                String googleResponse = restTemplate.getForObject(googleUserInfoUri, String.class);
                return objectMapper.readValue(googleResponse, new TypeReference<>() {});

            case "facebook":
                // Step 1: Exchange code for access token
                String tokenUri = UriComponentsBuilder
                        .fromUriString(facebookTokenUri)
                        .queryParam("client_id", facebookClientId)
                        .queryParam("redirect_uri", facebookRedirectUri)
                        .queryParam("client_secret", facebookClientSecret)
                        .queryParam("code", code)
                        .toUriString();

                ResponseEntity<String> tokenResponse = restTemplate.getForEntity(tokenUri, String.class);
                JsonNode jsonNode = objectMapper.readTree(tokenResponse.getBody());
                String fbAccessToken = jsonNode.get("access_token").asText();

                // Step 2: Fetch user profile
                String userInfoUri = facebookUserInfoUri + "&access_token=" + fbAccessToken;
                String fbResponse = restTemplate.getForObject(userInfoUri, String.class);
                return objectMapper.readValue(fbResponse, new TypeReference<>() {});

            default:
                log.warn("Unsupported login type: {}", loginType);
                return Collections.emptyMap();
        }
    }

}

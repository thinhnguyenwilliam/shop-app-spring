package com.example.shopapp.service;

import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);

    Token refreshToken(String refreshToken, User user) throws Exception;

}

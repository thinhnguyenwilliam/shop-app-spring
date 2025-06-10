package com.example.shopapp.service;

import com.example.shopapp.models.User;

public interface ITokenService {
    void addToken(User user, String token, boolean isMobileDevice);
}

package com.example.shopapp.service;


import com.example.shopapp.dtos.request.UserLoginDTO;

import java.io.IOException;
import java.util.Map;

public interface IAuthService {
    String generateAuthUrl(String loginType);

    UserLoginDTO authenticateSocialUser(String code, String loginType) throws IOException;

    Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException;
}



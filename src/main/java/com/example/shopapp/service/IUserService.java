package com.example.shopapp.service;

import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.User;

public interface IUserService {
    User createUser(UserDTO userDTO) throws DataNotFoundException;

    String login(String phoneNumber, String password, Integer roleId);

    User getUserDetailsFromToken(String extractedToken) throws Exception;

    User updateUser(Integer userId, UserDTO userDTO);

    User getUserDetailsFromRefreshToken(String token) throws Exception;
}

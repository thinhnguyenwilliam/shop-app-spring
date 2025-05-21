package com.example.shopapp.service;

import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;

public interface IUserService {
    void createUser(UserDTO userDTO) throws DataNotFoundException;

    String login(String phoneNumber, String password);
}

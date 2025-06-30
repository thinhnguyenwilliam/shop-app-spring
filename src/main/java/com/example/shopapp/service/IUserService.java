package com.example.shopapp.service;

import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidPasswordException;
import com.example.shopapp.models.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    void blockOrEnable(Integer userId, Boolean active) throws DataNotFoundException;

    void resetPassword(Integer userId, String newPassword)
            throws InvalidPasswordException, DataNotFoundException ;

    User createUser(UserDTO userDTO) throws DataNotFoundException;

    String login(String phoneNumber, String email, String password, Integer roleId);

    User getUserDetailsFromToken(String extractedToken) throws Exception;

    User updateUser(Integer userId, UserDTO userDTO);

    User getUserDetailsFromRefreshToken(String token) throws Exception;

    Page<User> findAll(String keyword, Pageable pageable) throws Exception;

    String loginSocial(UserLoginDTO userLoginDTO) throws Exception;
}

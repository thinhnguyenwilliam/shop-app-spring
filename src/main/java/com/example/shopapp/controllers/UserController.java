package com.example.shopapp.controllers;
import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.dtos.responses.LoginResponse;
import com.example.shopapp.models.User;
import com.example.shopapp.service.IUserService;
import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("/register")
    public ResponseEntity<Object> createUser(
            @Valid
            @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        try{
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + String.join(", ", errors));
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword()))
                return ResponseEntity.badRequest().body("Passwords do not match");

            User createdUser = userService.createUser(userDTO);
            return ResponseEntity.ok(createdUser);

        }catch(Exception e){
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Register failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
    ) {
        try {
            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId()
            );

            String message = localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY);

            return ResponseEntity.ok(
                    LoginResponse.builder()
                            .message(message)
                            .token(token)
                            .build()
            );
        } catch (Exception ex) {
            // Exception is just an example; use your actual exception class
            String errorMessage = localizationUtils.getLocalizedMessage(
                    MessageKeys.LOGIN_FAILED,
                    new Object[]{ex.getMessage()} // Pass the exception message as an argument
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .message(errorMessage)
                            .token(null)
                            .build()
                    );
        }
    }



}

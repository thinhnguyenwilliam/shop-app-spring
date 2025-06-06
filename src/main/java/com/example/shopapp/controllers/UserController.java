package com.example.shopapp.controllers;
import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.dtos.responses.LoginResponse;
import com.example.shopapp.dtos.responses.UserResponse;
import com.example.shopapp.models.User;
import com.example.shopapp.service.IUserService;
import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
            int roleId = userLoginDTO.getRoleId() == null ? 2 : userLoginDTO.getRoleId();
            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    roleId
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

    @PostMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(@RequestHeader("Authorization") String token) {
        try {
            String extractedToken = token.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/details/{userId}")
    public ResponseEntity<UserResponse> updateUserDetails(
            @PathVariable("userId") Integer userId,
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User tokenUser = userService.getUserDetailsFromToken(extractedToken);

            if (!tokenUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            User updatedUser = userService.updateUser(userId, userDTO);
            return ResponseEntity.ok(UserResponse.fromUser(updatedUser));

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}

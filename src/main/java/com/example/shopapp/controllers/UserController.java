package com.example.shopapp.controllers;
import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.dtos.responses.LoginResponse;
import com.example.shopapp.models.User;
import com.example.shopapp.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final MessageSource messageSource;

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
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request
    ) {
        String token = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("user.login.login_successfully", null, locale);

        return ResponseEntity.ok(
                LoginResponse.builder()
                        .message(message)
                        .token(token)
                        .build()
        );
    }


}

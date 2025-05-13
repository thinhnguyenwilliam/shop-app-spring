package com.example.shopapp.controllers;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.dtos.UserLoginDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
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

            return ResponseEntity.ok("Register success");
        }catch(Exception e){
            return ResponseEntity.badRequest().body("Register failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {

        return ResponseEntity.ok("Some Token");
    }

}

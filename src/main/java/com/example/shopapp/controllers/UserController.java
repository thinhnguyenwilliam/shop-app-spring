package com.example.shopapp.controllers;
import com.example.shopapp.dtos.request.RefreshTokenDTO;
import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.dtos.responses.LoginResponse;
import com.example.shopapp.dtos.responses.ResponseObject;
import com.example.shopapp.dtos.responses.UserListResponse;
import com.example.shopapp.dtos.responses.UserResponse;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidPasswordException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.service.IAuthService;
import com.example.shopapp.service.ITokenService;
import com.example.shopapp.service.IUserService;
import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.utils.MessageKeys;
import com.example.shopapp.utils.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;
    private final IAuthService authService;

    // Step 1: Generate Google Auth URL (Angular frontend calls this)
    @GetMapping("/auth/social-login")
    public ResponseEntity<String> socialAuth(@RequestParam("login_type") String loginType) {
        String url = authService.generateAuthUrl(loginType);
        return ResponseEntity.ok(url);
    }

    // Step 2: Handle the Google callback (user is redirected here after login)
    @GetMapping("/auth/social/callback")
    public ResponseEntity<ResponseObject> callback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType,
            HttpServletRequest request
    ) throws Exception {
        UserLoginDTO userLoginDTO = authService.authenticateSocialUser(code, loginType);
        return this.loginSocial(userLoginDTO, request);
    }

    // Step 3: Login user and return token (you can customize this further)
    private ResponseEntity<ResponseObject> loginSocial(
            @Valid UserLoginDTO userLoginDTO,
            HttpServletRequest request
    ) throws Exception {
        // Gọi hàm loginSocial từ UserService cho đăng nhập mạng xã hội
        String token = userService.loginSocial(userLoginDTO);

        // Xử lý token và thông tin người dùng
        String userAgent = request.getHeader("User-Agent");
        User userDetail = userService.getUserDetailsFromToken(token);
        Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

        // Tạo đối tượng LoginResponse
        LoginResponse loginResponse = LoginResponse.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .refreshToken(jwtToken.getRefreshToken())
                .username(userDetail.getPhoneNumber())
                .roles(List.of("ROLE_" + userDetail.getRole().getName().toUpperCase()))
                .id(userDetail.getId())
                .build();

        // Trả về phản hồi
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Login successfully")
                        .data(loginResponse)
                        .status(HttpStatus.OK)
                        .build()
        );
    }



    @PutMapping("/block/{userId}/{active}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> blockOrEnable(
            @PathVariable Integer userId,
            @PathVariable int active
    ) {
        try {
            userService.blockOrEnable(userId, active > 0);
            String message = active > 0 ? "User enabled successfully." : "User blocked successfully.";
            return ResponseEntity.ok(Map.of("message", message));
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/reset-password/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> resetPassword(@Valid @PathVariable Integer userId) {
        try {
            String newPassword = generateRandomPassword();
            userService.resetPassword(userId, newPassword);
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully",
                    "newPassword", newPassword
            ));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid password"));
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    //use a custom random password generator with letters + digits:
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }



    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        try {
            // Tạo Pageable từ thông tin trang và giới hạn
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    //Sort.by("createdAt").descending()
                    Sort.by("id").ascending()
            );
            Page<UserResponse> userPage = userService.findAll(keyword, pageRequest)
                    .map(UserResponse::fromUser);

            // Lấy tổng số trang
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();
            return ResponseEntity.ok(UserListResponse
                    .builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO
    ) {
        try {
            User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
            return ResponseEntity.ok(LoginResponse.builder()
                    .message("Refresh token successfully")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(userDetail.getPhoneNumber())
                    .roles(List.of("ROLE_" + userDetail.getRole().getName().toUpperCase()))
                    .id(userDetail.getId())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .message("refresh token fail")
                            .build()
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        try {
            // Map to hold all validation errors
            Map<String, String> errors = new HashMap<>();

            // 1. Add errors from @Valid annotation-based validation
            result.getFieldErrors().forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

            // 2. Add custom manual validation errors
            if (!ValidationUtils.isValidEmail(userDTO.getEmail())) {
                errors.put("email", "Invalid email format");
            }

            if (!ValidationUtils.isValidPhoneNumber(userDTO.getPhoneNumber())) {
                errors.put("phoneNumber", "Invalid phone number format");
            }

            if (!ValidationUtils.isValidPassword(userDTO.getPassword())) {
                errors.put("password", "Password must be at least 3 characters");
            }

            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                errors.put("retypePassword", "Passwords do not match");
            }

            // 3. Return error response if any exist
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }

            // 4. Proceed with creating user
            User createdUser = userService.createUser(userDTO);
            return ResponseEntity.ok(createdUser);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Register failed: " + e.getMessage())
            );
        }
    }



    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request
    ) {
        try {
            int roleId = (userLoginDTO.getRoleId() != null) ? userLoginDTO.getRoleId() : 2;

            String userAgent = request.getHeader("User-Agent");
            boolean isMobile = isMobileDevice(userAgent);

            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getEmail(),
                    roleId
            );

            User user = userService.getUserDetailsFromToken(token);
            Token jwtToken = tokenService.addToken(user, token, isMobile);

            String message = localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY);

            LoginResponse loginResponse = LoginResponse.builder()
                    .message(message)
                    .token(token)
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(user.getPhoneNumber())
                    .roles(List.of("ROLE_" + user.getRole().getName().toUpperCase()))
                    .id(user.getId())
                    .build();

            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Login successful")
                    .status(HttpStatus.OK)
                    .data(loginResponse)
                    .build());

        } catch (Exception ex) {
            String errorMessage = localizationUtils.getLocalizedMessage(
                    MessageKeys.LOGIN_FAILED,
                    new Object[]{ex.getMessage()}
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseObject.builder()
                            .message(errorMessage)
                            .status(HttpStatus.UNAUTHORIZED)
                            .data(null)
                            .build());
        }
    }


    private boolean isMobileDevice(String userAgent) {
        if (userAgent == null) return false;

        String ua = userAgent.toLowerCase();
        return ua.contains("mobile") || ua.contains("android") || ua.contains("iphone");
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

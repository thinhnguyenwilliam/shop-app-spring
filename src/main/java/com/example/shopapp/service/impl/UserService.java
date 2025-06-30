package com.example.shopapp.service.impl;

import com.example.shopapp.components.JwtTokenUtil;
import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.dtos.request.UserLoginDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidPasswordException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.service.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService
{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final LocalizationUtils localizationUtils;

    @Override
    @Transactional
    public void blockOrEnable(Integer userId, Boolean active) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        existingUser.setIsActive(active);
        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void resetPassword(Integer userId, String newPassword)
            throws InvalidPasswordException, DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        //reset password => clear token
        List<Token> tokens = tokenRepository.findByUser(existingUser);
        tokenRepository.deleteAll(tokens);
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws DataNotFoundException {
        String phoneNumber = userDTO.getPhoneNumber();

        if (Boolean.TRUE.equals(userRepository.existsByPhoneNumber(phoneNumber))) {
            throw new DataIntegrityViolationException("User already exists with phone number: " + phoneNumber);
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found for id: " + userDTO.getRoleId()));

        if(role.getName().equals("ADMIN")){
            throw new DataIntegrityViolationException("Admin role cannot be created manually");
        }

        // Determine if this is a normal (non-social) user
        boolean isNormalAccount = (userDTO.getFacebookAccountId() == null || userDTO.getFacebookAccountId().isEmpty())
                && (userDTO.getGoogleAccountId() == null || userDTO.getGoogleAccountId().isEmpty());

        String rawPassword = userDTO.getPassword();
        String encodedPassword = isNormalAccount ? passwordEncoder.encode(rawPassword) : rawPassword;

        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(phoneNumber)
                .password(encodedPassword)
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .role(role)
                .email(userDTO.getEmail() != null ? userDTO.getEmail().toLowerCase() : null)
                .isActive(true)
                .build();

        userRepository.save(newUser);
        return newUser;
    }

    @Override
    public String login(String phoneNumber, String password, String email, Integer roleId) {
        Optional<User> optionalUser = Optional.empty();
        String subject = null;

        // 1. Try to find user by phone number
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            optionalUser = userRepository.findByPhoneNumber(phoneNumber);
            subject = phoneNumber;
        }

        // 2. If not found by phone, try by email
        if (optionalUser.isEmpty() && email != null && !email.isBlank()) {
            optionalUser = userRepository.findByEmail(email);
            subject = email;
        }

        // 3. If still not found, throw exception
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();

        if(Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("User is not active");
        }

        // 4. Validate role
        roleRepository.findById(roleId).orElseThrow(
                () -> new RuntimeException("Role not found for id: " + roleId)
        );

        if (!roleId.equals(user.getRole().getId())) {
            throw new RuntimeException("User does not have the specified role.");
        }

        // 5. Validate password (only for non-social logins)
        boolean isNormalUser = user.getFacebookAccountId().isEmpty() && user.getGoogleAccountId().isEmpty();

        if (isNormalUser && !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // 6. Authenticate the user
        if (isNormalUser) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(subject, password);
            authenticationManager.authenticate(authenticationToken);
        }

        // 7. Generate and return JWT token
        return jwtTokenUtil.generateToken(user);
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if (jwtTokenUtil.isTokenExpired(token)) {
            throw new Exception("Token is expired");
        }

        String phoneNumber = jwtTokenUtil.getPhoneNumberFromToken(token);

        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new Exception("User not found"));
    }

    @Override
    @Transactional
    public User updateUser(Integer userId, UserDTO updateUserDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        existingUser.setFullName(updateUserDTO.getFullName());
        existingUser.setAddress(updateUserDTO.getAddress());
        existingUser.setDateOfBirth(updateUserDTO.getDateOfBirth());

        // Encode and update password only if provided
        String encodedPassword = passwordEncoder.encode(updateUserDTO.getPassword());
        existingUser.setPassword(encodedPassword);

        return userRepository.save(existingUser);

    }

    @Override
    public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        return getUserDetailsFromToken(existingToken.getToken());
    }

    @Override
    public Page<User> findAll(String keyword, Pageable pageable) {
        return userRepository.findAll(keyword, pageable);
    }

    @Override
    public String loginSocial(UserLoginDTO userLoginDTO) throws Exception {
        // 1. Get user role (usually "USER")
        Role roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role 'USER' not found"));

        User user;

        if (userLoginDTO.isGoogleAccountIdValid()) {
            // 2. Try to find existing user by Google account ID
            Optional<User> optionalUser = userRepository.findByGoogleAccountId(userLoginDTO.getGoogleAccountId());

            user = optionalUser.orElseGet(() -> {
                // 3. Create new user if not found
                User newUser = User.builder()
                        .phoneNumber("")
                        .fullName(Optional.ofNullable(userLoginDTO.getFullName()).orElse(""))
                        .email(Optional.ofNullable(userLoginDTO.getEmail()).orElse(""))
                        .profileImage(Optional.ofNullable(userLoginDTO.getProfileImage()).orElse(""))
                        .role(roleUser)
                        .googleAccountId(userLoginDTO.getGoogleAccountId())
                        .password("") // No password for social login
                        .isActive(true)
                        .build();

                return userRepository.save(newUser);
            });

        } else if (userLoginDTO.isFacebookAccountIdValid()) {
            // 2. Try to find existing user by Facebook account ID
            Optional<User> optionalUser = userRepository.findByFacebookAccountId(userLoginDTO.getFacebookAccountId());

            user = optionalUser.orElseGet(() -> {
                // 3. Create new user if not found
                User newUser = User.builder()
                        .phoneNumber("")
                        .fullName(Optional.ofNullable(userLoginDTO.getFullName()).orElse(""))
                        .email(Optional.ofNullable(userLoginDTO.getEmail()).orElse(""))
                        .profileImage(Optional.ofNullable(userLoginDTO.getProfileImage()).orElse(""))
                        .role(roleUser)
                        .facebookAccountId(userLoginDTO.getFacebookAccountId())
                        .password("")
                        .isActive(true)
                        .build();

                return userRepository.save(newUser);
            });

        } else {
            throw new IllegalArgumentException("No valid social account ID found.");
        }

        // 4. Check if user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("User account is inactive or locked.");
        }

        // 5. Generate and return JWT token
        return jwtTokenUtil.generateToken(user);
    }




}

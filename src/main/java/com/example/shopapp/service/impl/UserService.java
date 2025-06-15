package com.example.shopapp.service.impl;

import com.example.shopapp.components.JwtTokenUtil;
import com.example.shopapp.dtos.request.UserDTO;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
        boolean isNormalAccount = (userDTO.getFacebookAccountId() == null || userDTO.getFacebookAccountId() == 0)
                && (userDTO.getGoogleAccountId() == null || userDTO.getGoogleAccountId() == 0);

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
    public String login(String phoneNumber, String password, Integer roleId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found for phone number: " + phoneNumber));

        // Check password (assume it's stored securely with encoder)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password.");
        }

        // Validate role
        roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found for id: " + roleId));

        if (!roleId.equals(user.getRole().getId())) {
            throw new RuntimeException("User does not have the specified role.");
        }

        // Optional: You can check if it's a normal account (non-social login)
        boolean isNormalUser = (user.getFacebookAccountId() == 0 && user.getGoogleAccountId() == 0);

        if (isNormalUser) {
            // Authenticate using Spring Security
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(phoneNumber, password);
            authenticationManager.authenticate(authenticationToken);
        }

        // Generate and return JWT token
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


}

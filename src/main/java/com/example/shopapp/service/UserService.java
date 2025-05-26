package com.example.shopapp.service;

import com.example.shopapp.components.JwtTokenUtil;
import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService
{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
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
    public String login(String phoneNumber, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found for phone number: " + phoneNumber));

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


}

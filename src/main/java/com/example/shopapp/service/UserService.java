package com.example.shopapp.service;

import com.example.shopapp.dtos.request.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService
{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void createUser(UserDTO userDTO) throws DataNotFoundException {
        String phoneNumber = userDTO.getPhoneNumber();

        if (Boolean.TRUE.equals(userRepository.existsByPhoneNumber(phoneNumber))) {
            throw new DataIntegrityViolationException("User already exists with phone number: " + phoneNumber);
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found for id: " + userDTO.getRoleId()));

        // Determine if this is a normal (non-social) user
        boolean isNormalAccount = (userDTO.getFacebookAccountId() == null || userDTO.getFacebookAccountId() == 0)
                && (userDTO.getGoogleAccountId() == null || userDTO.getGoogleAccountId() == 0);

        // Optional: hash password only for normal users
        String password = userDTO.getPassword();
        if (isNormalAccount) {
            // e.g., password = passwordEncoder.encode(password);
        }

        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(phoneNumber)
                .password(password)
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .role(role)
                //.isActive(true)
                .build();

        userRepository.save(newUser);
    }



    @Override
    public String login(String phoneNumber, String password) {
        return null;
    }
}

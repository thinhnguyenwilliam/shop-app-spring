package com.example.shopapp.service.impl;

import com.example.shopapp.models.User;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByPhoneNumber(login);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(login);
        }

        User user = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("User not found with phone or email: " + login)
        );

        return new CustomUserDetails(user, login); // ✅ done here
    }
}


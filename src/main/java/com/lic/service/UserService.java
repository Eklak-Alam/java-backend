package com.lic.service;

import com.lic.dto.UserRegistrationDto;
import com.lic.entities.User;
import com.lic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {  // changed from getUsername()
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {  // changed from getEmail()
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());  // changed from getUsername()
        user.setEmail(registrationDto.getEmail());  // changed from getEmail()
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));  // changed from getPassword()
        user.setRole("USER"); // Default role

        return userRepository.save(user);
    }

    public User promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole("ADMIN");
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
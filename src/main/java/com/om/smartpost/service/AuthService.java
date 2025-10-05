package com.om.smartpost.service;

import com.om.smartpost.dto.request.LoginReq;
import com.om.smartpost.dto.request.RegisterReq;
import com.om.smartpost.dto.response.AuthResponse;
import com.om.smartpost.entity.User;
import com.om.smartpost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterReq request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, null, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginReq request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            // Find user by identifier
            User user = userRepository.findByEmail(request.getIdentifier())
                    .or(() -> userRepository.findByUsername(request.getIdentifier()))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate JWT token
            String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

            return new AuthResponse(token, null, user.getUsername(), user.getRole());
        } else {
            throw new UsernameNotFoundException("Invalid credentials");
        }
    }
}

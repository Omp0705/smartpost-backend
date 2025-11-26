package com.om.smartpost.service;

import com.om.smartpost.dto.request.LoginReq;
import com.om.smartpost.dto.request.RegisterReq;
import com.om.smartpost.dto.response.AuthResponse;
import com.om.smartpost.dto.response.SignUpResponse;
import com.om.smartpost.entity.RefreshToken;
import com.om.smartpost.entity.User;
import com.om.smartpost.error.ErrorCodes;
import com.om.smartpost.exception.ConstraintViolationTranslator;
import com.om.smartpost.exception.DuplicateResourceException;
import com.om.smartpost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final RefreshTokenService refreshTokenService;

    public ResponseEntity<Object> register(RegisterReq request) {
        // Pre-checks to return friendly errors quickly (still catch DB race conditions later)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(ErrorCodes.USERNAME_EXISTS.toString(), "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(ErrorCodes.EMAIL_EXISTS.toString(), "Email already exists");
        }
        if (userRepository.existsByMobileNo(request.getMobileNo())) {
            throw new DuplicateResourceException(ErrorCodes.MOBILE_EXISTS.toString(),"Mobile No alredy exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Race condition fallback — try to translate DB constraint to a known error code
            ErrorCodes code = ConstraintViolationTranslator.toErrorCode(ex).orElse(ErrorCodes.DUPLICATE_RESOURCE);
            String message = switch (code) {
                case USERNAME_EXISTS -> "Username already exists";
                case EMAIL_EXISTS -> "Email already exists";
                default -> "Resource conflict";
            };
            throw new DuplicateResourceException(code.toString(), message, ex);
        }


        return ResponseEntity.status(HttpStatus.CREATED).body(new SignUpResponse("Profile created successfully"));
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
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());
            return new AuthResponse(token,refreshToken.getToken() , user.getUsername(), user.getRole());
        } else {
            throw new UsernameNotFoundException("Invalid credentials");
        }
    }
}

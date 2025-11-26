package com.om.smartpost.controller;

import com.om.smartpost.dto.request.LoginReq;
import com.om.smartpost.dto.request.RegisterReq;
import com.om.smartpost.dto.request.TokenRefreshReq;
import com.om.smartpost.dto.response.AuthResponse;
import com.om.smartpost.dto.response.ProfileResponse;
import com.om.smartpost.entity.RefreshToken;
import com.om.smartpost.entity.User;
import com.om.smartpost.repository.UserRepository;
import com.om.smartpost.service.AuthService;
import com.om.smartpost.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    // Public endpoint: Register new users
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterReq request) {
        return authService.register(request);
    }

    // Public endpoint: Login via email or username
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginReq request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody TokenRefreshReq request) {
        return ResponseEntity.ok(refreshTokenService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String,String>> validate() {
        Map<String, String> response = new HashMap<>();
        response.put("msg", "valid token");
        return ResponseEntity.ok(response);
    }
    // Example protected endpoint for role validation
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // usually the principal username

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponse profile = new ProfileResponse(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(profile);
    }

    // Example RBAC enforcement
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome, Post Admin!");
    }

    @GetMapping("/postman/routes")
    @PreAuthorize("hasRole('POSTMAN')")
    public ResponseEntity<String> postmanRoutes() {
        return ResponseEntity.ok("Welcome, Postman!");
    }

    @GetMapping("/receiver/home")
    @PreAuthorize("hasRole('RECEIVER')")
    public ResponseEntity<String> receiverHome() {
        return ResponseEntity.ok("Welcome, Receiver!");
    }
}


package com.om.smartpost.controller;

import com.om.smartpost.dto.request.LoginReq;
import com.om.smartpost.dto.request.RegisterReq;
import com.om.smartpost.dto.request.TokenRefreshReq;
import com.om.smartpost.dto.response.AuthResponse;
import com.om.smartpost.entity.RefreshToken;
import com.om.smartpost.service.AuthService;
import com.om.smartpost.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    // Public endpoint: Register new users
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterReq request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
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

    // Example protected endpoint for role validation
    @GetMapping("/profile")
    public ResponseEntity<String> profile() {
        return ResponseEntity.ok("Login success. Token is valid!");
    }

    // Example RBAC enforcement
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('POST_ADMIN')")
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


package com.om.smartpost.auth.controller;

import com.om.smartpost.auth.dto.request.LoginRequest;
import com.om.smartpost.auth.dto.request.OtpVerifyRequest;
import com.om.smartpost.auth.dto.request.RegisterRequest;
import com.om.smartpost.auth.dto.request.ResetPasswordRequest;
import com.om.smartpost.auth.dto.request.TokenRefreshRequest;
import com.om.smartpost.auth.dto.response.AuthResponse;
import com.om.smartpost.auth.dto.response.GenericResponse;
import com.om.smartpost.auth.dto.response.ProfileResponse;
import com.om.smartpost.auth.dto.response.SignUpResponse;
import com.om.smartpost.auth.service.AuthService;
import com.om.smartpost.auth.service.RefreshTokenService;
import com.om.smartpost.core.error.ForgotPasswordError;
import com.om.smartpost.core.security.AuthenticatedUserFacade;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @PostMapping("/register")
    public ResponseEntity<SignUpResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/guest/login")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String mobileNo = request.get("mobileNo");
        String status = authService.sendGuestOtp(mobileNo);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "status", status));
    }

    @PostMapping("/guest/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String mobileNo = request.get("mobileNo");
            String otp = request.get("otp");
            Map<String, String> authData = authService.verifyAndLoginGuest(mobileNo, otp);
            return ResponseEntity.ok(authData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validate() {
        Map<String, String> response = new HashMap<>();
        response.put("msg", "valid token");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(Authentication authentication) {
        var user = authenticatedUserFacade.currentUser(authentication);
        return ResponseEntity.ok(new ProfileResponse(user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) throws MessagingException {
        String email = request.get("email");
        Map<String, String> response = authService.forgotPassword(email);
        return ResponseEntity.ok(new GenericResponse<>(ForgotPasswordError.SUCCESS, response.get("msg")));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<GenericResponse<ForgotPasswordError>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request.getEmail(), Integer.valueOf(request.getOtp()));
        return ResponseEntity.ok(new GenericResponse<>(ForgotPasswordError.SUCCESS, "Otp Verified"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse<ForgotPasswordError>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(new GenericResponse<>(ForgotPasswordError.SUCCESS, "Password Reset Successfully"));
    }
}

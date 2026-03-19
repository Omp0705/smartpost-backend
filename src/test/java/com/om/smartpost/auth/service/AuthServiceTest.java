package com.om.smartpost.auth.service;

import com.om.smartpost.auth.dto.request.LoginRequest;
import com.om.smartpost.auth.dto.request.RegisterRequest;
import com.om.smartpost.auth.dto.response.AuthResponse;
import com.om.smartpost.auth.entity.RefreshToken;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.auth.repository.PasswordResetTokenRepository;
import com.om.smartpost.core.identity.repository.UserRepository;
import com.om.smartpost.core.notification.EmailSender;
import com.om.smartpost.core.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailSender emailSender;
    @Mock
    private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                refreshTokenService,
                passwordResetTokenRepository,
                emailSender
        );
    }

    @Test
    void registerRejectsSuperAdminRoleFromPublicEndpoint() {
        RegisterRequest request = new RegisterRequest(
                "System Admin",
                "superadmin",
                "superadmin@smartpost.local",
                "9999999999",
                "superadmin123",
                UserRole.SUPERADMIN
        );

        ApiException exception = assertThrows(ApiException.class, () -> authService.register(request));

        assertEquals("FORBIDDEN", exception.getCode());
        assertEquals("This role must be created by an authorized administrator", exception.getMessage());
    }

    @Test
    void loginReturnsExistingTokenPayloadForSuperAdmin() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("superadmin");
        request.setPassword("superadmin123");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("superadmin");
        user.setRole(UserRole.SUPERADMIN);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("superadmin")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("superadmin", "SUPERADMIN")).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("superadmin", response.getUsername());
        assertEquals(UserRole.SUPERADMIN, response.getRole());
    }
}



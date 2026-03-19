package com.om.smartpost.auth.service;

import com.om.smartpost.core.config.TwilioConfig;
import com.om.smartpost.auth.dto.request.LoginRequest;
import com.om.smartpost.auth.dto.request.RegisterRequest;
import com.om.smartpost.auth.dto.response.AuthResponse;
import com.om.smartpost.auth.dto.response.SignUpResponse;
import com.om.smartpost.auth.entity.PasswordResetToken;
import com.om.smartpost.auth.entity.RefreshToken;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.core.exception.ConstraintViolationTranslator;
import com.om.smartpost.core.exception.DuplicateResourceException;
import com.om.smartpost.auth.repository.PasswordResetTokenRepository;
import com.om.smartpost.core.identity.repository.UserRepository;
import com.om.smartpost.core.notification.EmailSender;
import com.om.smartpost.core.security.JwtService;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Autowired
    private TwilioConfig twilioConfig;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSender emailService;

    public SignUpResponse register(RegisterRequest request) {
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
        user.setRole(resolvePublicRegistrationRole(request));
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


        return new SignUpResponse("Profile created successfully");
    }


    public AuthResponse login(LoginRequest request) {
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

    public String sendGuestOtp(String mobileNo) {
        try {
            Verification verification = Verification.creator(
                    twilioConfig.getServiceSid(),
                    mobileNo,  // (e.g., +91XXXXXXXXXX)
                    "sms"
            ).create();

            log.info("OTP sent to {}. Status: {}", mobileNo, verification.getStatus());
            return verification.getStatus(); // Usually returns "pending"
        } catch (Exception e) {
            log.error("Twilio error: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP");
        }
    }

    public Map<String, String> verifyAndLoginGuest(String mobileNo, String otp) {
        String formattedMobile = mobileNo.startsWith("+") ? mobileNo : "+91" + mobileNo;

        VerificationCheck check = VerificationCheck.creator(twilioConfig.getServiceSid())
                .setTo(formattedMobile)
                .setCode(otp)
                .create();

        if (!"approved".equals(check.getStatus())) {
            throw new RuntimeException("Invalid or Expired OTP");
        }

        // Logic to find existing user or create a new Guest
        User user = userRepository.findByMobileNo(mobileNo)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setMobileNo(mobileNo);
                    // Use only digits for username to keep it clean
                    newUser.setUsername("guest_" + mobileNo.replaceAll("[^0-9]", ""));
                    newUser.setRole(UserRole.GUEST);
                    newUser.setIsGuest(true);
                    newUser.setIsActive(true);
                    newUser.setGuestExpiry(LocalDateTime.now().plusDays(7));
                    return userRepository.save(newUser);
                });

        // Generate Token using your JwtService
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());

        return response;
    }

//    For Forgot password (Step 1: Email send )
    public Map<String,String> forgotPassword(String email) throws MessagingException {
        Optional<User> user = userRepository.findByEmail(email);
        Map<String,String> response = new HashMap<>();

//        For safety purpose
        if(user.isEmpty()) {
            response.put("msg","Email sent to the registered");
            return response;
        }
        int otp  = (int) (Math.random() * 9000)+ 1000;

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setEmail(email);
        passwordResetToken.setOtp(otp);
        passwordResetToken.setExpiry(LocalDateTime.now().plusMinutes(5));
        passwordResetTokenRepository.save(passwordResetToken);

        String htmlTemplate = """
        <h2>Password Reset OTP</h2>
        <p>Your OTP for resetting your password is:</p>
        <h1 style="letter-spacing: 4px;">${OTP}</h1>
        <p>Valid for 5 minutes.</p>
        """;

        emailService.sendForgotPasswordEmail(email, "Reset Password OTP", htmlTemplate.replace("${OTP}", String.valueOf(otp)));
        response.put("msg","Email sent to the registered");
        return response;
    }

    public void verifyOtp(String email, Integer otp) {
        PasswordResetToken token  = passwordResetTokenRepository
                .findByEmailAndOtpAndUsedFalse(email,otp)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Invalid OTP"));

        if (token.getExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "OTP expired");
        }

        token.setOtpVerified(true);
        passwordResetTokenRepository.save(token);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {

        // 1. Find the latest token for this email
        PasswordResetToken token = passwordResetTokenRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "No reset request found for this email"));

        // 2. Perform validations
        if (!token.isOtpVerified()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "OTP has not been verified yet");
        }

        if (token.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "This reset request has already been used");
        }

        if (token.getExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Reset session expired");
        }

        // 3. Find User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.USER_NOT_FOUND.toString(), "User not found"));

        // 4. Update Password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 5. Mark Token as Used
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }


    private UserRole resolvePublicRegistrationRole(RegisterRequest request) {
        if (request.getRole() == null || request.getRole() == UserRole.CUSTOMER) {
            return UserRole.CUSTOMER;
        }
        if (request.getRole() == UserRole.GUEST) {
            return UserRole.GUEST;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "This role must be created by an authorized administrator");
    }

}




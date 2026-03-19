package com.om.smartpost.core.config;

import com.om.smartpost.core.identity.entity.SuperAdmin;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.identity.repository.SuperAdminRepository;
import com.om.smartpost.core.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SuperAdminBootstrap {

    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;
    private final SuperAdminProperties superAdminProperties;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner bootstrapSuperAdmin() {
        return args -> {
            if (userRepository.existsByRole(UserRole.SUPERADMIN)) {
                return;
            }

            validateNoConflictingIdentity();

            User user = new User();
            user.setFullName(superAdminProperties.getFullName());
            user.setUsername(superAdminProperties.getUsername());
            user.setEmail(superAdminProperties.getEmail());
            user.setMobileNo(superAdminProperties.getMobileNo());
            user.setPasswordHash(passwordEncoder.encode(superAdminProperties.getPassword()));
            user.setRole(UserRole.SUPERADMIN);
            user.setIsActive(true);

            User savedUser = userRepository.save(user);

            SuperAdmin superAdmin = SuperAdmin.builder()
                    .user(savedUser)
                    .build();
            superAdminRepository.save(superAdmin);
        };
    }

    private void validateNoConflictingIdentity() {
        userRepository.findByUsername(superAdminProperties.getUsername())
                .ifPresent(user -> {
                    throw conflict("Configured superadmin username already belongs to another user");
                });

        userRepository.findByEmail(superAdminProperties.getEmail())
                .ifPresent(user -> {
                    throw conflict("Configured superadmin email already belongs to another user");
                });

        userRepository.findByMobileNo(superAdminProperties.getMobileNo())
                .ifPresent(user -> {
                    throw conflict("Configured superadmin mobile number already belongs to another user");
                });
    }

    private ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, ErrorCodes.DUPLICATE_RESOURCE.toString(), message);
    }
}




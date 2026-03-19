package com.om.smartpost.core.config;

import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.core.identity.repository.SuperAdminRepository;
import com.om.smartpost.core.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminBootstrapTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SuperAdminRepository superAdminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationArguments applicationArguments;

    private SuperAdminBootstrap superAdminBootstrap;

    @BeforeEach
    void setUp() {
        SuperAdminProperties properties = new SuperAdminProperties();
        properties.setFullName("System Super Admin");
        properties.setUsername("superadmin");
        properties.setEmail("superadmin@smartpost.local");
        properties.setMobileNo("9999999999");
        properties.setPassword("superadmin123");

        superAdminBootstrap = new SuperAdminBootstrap(
                userRepository,
                superAdminRepository,
                properties,
                passwordEncoder
        );
    }

    @Test
    void createsSuperAdminWhenMissing() throws Exception {
        when(userRepository.existsByRole(UserRole.SUPERADMIN)).thenReturn(false);
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("superadmin@smartpost.local")).thenReturn(Optional.empty());
        when(userRepository.findByMobileNo("9999999999")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });

        superAdminBootstrap.bootstrapSuperAdmin().run(applicationArguments);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserRole.SUPERADMIN, userCaptor.getValue().getRole());
        assertEquals("encoded-pass", userCaptor.getValue().getPasswordHash());
        verify(superAdminRepository).save(any());
    }

    @Test
    void skipsCreationWhenSuperAdminAlreadyExists() throws Exception {
        when(userRepository.existsByRole(UserRole.SUPERADMIN)).thenReturn(true);

        superAdminBootstrap.bootstrapSuperAdmin().run(applicationArguments);

        verify(userRepository, never()).save(any());
        verify(superAdminRepository, never()).save(any());
    }

    @Test
    void failsClearlyOnIdentityConflict() {
        when(userRepository.existsByRole(UserRole.SUPERADMIN)).thenReturn(false);
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(new User()));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> superAdminBootstrap.bootstrapSuperAdmin().run(applicationArguments)
        );

        assertEquals("DUPLICATE_RESOURCE", exception.getCode());
        assertEquals("Configured superadmin username already belongs to another user", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}


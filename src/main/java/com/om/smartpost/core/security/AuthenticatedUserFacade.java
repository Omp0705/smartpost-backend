package com.om.smartpost.core.security;

import com.om.smartpost.core.identity.CustomUserDetails;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.repository.UserRepository;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserFacade {

    private final UserRepository userRepository;

    public Long currentUserId(Authentication authentication) {
        return details(authentication).getUserId();
    }

    public UserRole currentUserRole(Authentication authentication) {
        return UserRole.valueOf(authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
    }

    public User currentUser(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.USER_NOT_FOUND.toString(), "Authenticated user not found"));
    }

    private CustomUserDetails details(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHENTICATED.toString(), "User is not authenticated");
        }
        return details;
    }
}

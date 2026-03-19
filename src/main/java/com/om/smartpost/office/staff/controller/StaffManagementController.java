package com.om.smartpost.office.staff.controller;

import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.security.AuthenticatedUserFacade;
import com.om.smartpost.office.staff.dto.request.PostAdminCreateRequest;
import com.om.smartpost.office.staff.dto.request.PostAdminUpdateRequest;
import com.om.smartpost.office.staff.dto.request.PostmanCreateRequest;
import com.om.smartpost.office.staff.dto.request.PostmanUpdateRequest;
import com.om.smartpost.office.staff.dto.response.PostAdminProfileResponse;
import com.om.smartpost.office.staff.dto.response.PostAdminResponse;
import com.om.smartpost.office.staff.dto.response.PostmanResponse;
import com.om.smartpost.office.staff.service.StaffManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffManagementController {

    private final StaffManagementService staffManagementService;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @PostMapping("/postadmins")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PostAdminResponse> createPostAdmin(@Valid @RequestBody PostAdminCreateRequest request) {
        return ResponseEntity.ok(staffManagementService.createPostAdmin(request));
    }

    @GetMapping("/postadmins")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<PostAdminResponse>> getPostAdmins() {
        return ResponseEntity.ok(staffManagementService.getPostAdmins());
    }

    @PutMapping("/postadmins/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PostAdminResponse> updatePostAdmin(@PathVariable UUID id, @Valid @RequestBody PostAdminUpdateRequest request) {
        return ResponseEntity.ok(staffManagementService.updatePostAdmin(id, request));
    }

    @DeleteMapping("/postadmins/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deletePostAdmin(@PathVariable UUID id) {
        staffManagementService.deletePostAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/postmen")
    @PreAuthorize("hasAnyRole('POSTADMIN','SUPERADMIN')")
    public ResponseEntity<PostmanResponse> createPostman(Authentication authentication, @Valid @RequestBody PostmanCreateRequest request) {
        return ResponseEntity.ok(staffManagementService.createPostman(
                authenticatedUserFacade.currentUserId(authentication),
                authenticatedUserFacade.currentUserRole(authentication),
                request));
    }

    @GetMapping("/postmen")
    @PreAuthorize("hasAnyRole('POSTADMIN','SUPERADMIN')")
    public ResponseEntity<List<PostmanResponse>> getPostmen(Authentication authentication, @RequestParam(required = false) UUID officeId) {
        UserRole role = authenticatedUserFacade.currentUserRole(authentication);
        if (role == UserRole.POSTADMIN) {
            PostAdminProfileResponse profile = staffManagementService.getAdminProfile(authenticatedUserFacade.currentUserId(authentication));
            return ResponseEntity.ok(staffManagementService.getPostmen(profile.officeId()));
        }
        return ResponseEntity.ok(staffManagementService.getPostmen(officeId));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('POSTADMIN')")
    public ResponseEntity<PostAdminProfileResponse> getMyProfile(Authentication authentication) {
        Long userId = authenticatedUserFacade.currentUserId(authentication);
        log.info("Fetching profile for admin user ID: {}", userId);
        return ResponseEntity.ok(staffManagementService.getAdminProfile(userId));
    }

    @PutMapping("/postmen/{id}")
    @PreAuthorize("hasAnyRole('POSTADMIN','SUPERADMIN')")
    public ResponseEntity<PostmanResponse> updatePostman(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody PostmanUpdateRequest request) {
        return ResponseEntity.ok(staffManagementService.updatePostman(
                id,
                authenticatedUserFacade.currentUserId(authentication),
                authenticatedUserFacade.currentUserRole(authentication),
                request));
    }

    @DeleteMapping("/postmen/{id}")
    @PreAuthorize("hasAnyRole('POSTADMIN','SUPERADMIN')")
    public ResponseEntity<Void> deletePostman(Authentication authentication, @PathVariable UUID id) {
        staffManagementService.deletePostman(id, authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication));
        return ResponseEntity.noContent().build();
    }
}

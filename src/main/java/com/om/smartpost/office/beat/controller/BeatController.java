package com.om.smartpost.office.beat.controller;

import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.security.AuthenticatedUserFacade;
import com.om.smartpost.office.beat.dto.request.BeatAssignmentRequest;
import com.om.smartpost.office.beat.dto.request.BeatRequest;
import com.om.smartpost.office.beat.dto.response.BeatResponse;
import com.om.smartpost.office.beat.service.BeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beats")
@RequiredArgsConstructor
public class BeatController {

    private final BeatService beatService;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @PostMapping
    @PreAuthorize("hasAnyRole('POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<BeatResponse> create(Authentication authentication, @Valid @RequestBody BeatRequest request) {
        return ResponseEntity.ok(
                beatService.create(authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication), request)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('POSTMAN', 'POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<BeatResponse>> getAll(Authentication authentication, @RequestParam(required = false) UUID officeId) {
        return ResponseEntity.ok(
                beatService.getAll(authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication), officeId)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('POSTMAN', 'POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<BeatResponse> getById(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(
                beatService.getById(id, authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<BeatResponse> update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody BeatRequest request) {
        return ResponseEntity.ok(
                beatService.update(id, authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication), request)
        );
    }

    @PutMapping("/{id}/assign-postman")
    @PreAuthorize("hasAnyRole('POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<BeatResponse> assignPostman(Authentication authentication, @PathVariable UUID id, @RequestBody BeatAssignmentRequest request) {
        return ResponseEntity.ok(
                beatService.assignPostman(id, authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication), request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        beatService.delete(id, authenticatedUserFacade.currentUserId(authentication), authenticatedUserFacade.currentUserRole(authentication));
        return ResponseEntity.noContent().build();
    }
}

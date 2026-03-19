package com.om.smartpost.office.dashboard.controller;

import com.om.smartpost.office.dashboard.dto.response.DashboardStatsResponse;
import com.om.smartpost.core.security.AuthenticatedUserFacade;
import com.om.smartpost.office.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<DashboardStatsResponse> getStats(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getStats(
                authenticatedUserFacade.currentUserId(authentication),
                authenticatedUserFacade.currentUserRole(authentication)
        ));
    }
}

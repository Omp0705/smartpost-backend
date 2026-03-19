package com.om.smartpost.shipment.controller;

import com.om.smartpost.core.security.AuthenticatedUserFacade;
import com.om.smartpost.office.staff.dto.response.PostAdminProfileResponse;
import com.om.smartpost.office.staff.service.StaffManagementService;
import com.om.smartpost.shipment.dto.request.AssignPostmanRequest;
import com.om.smartpost.shipment.dto.request.ShipmentCreateRequest;
import com.om.smartpost.shipment.dto.request.StatusUpdateRequest;
import com.om.smartpost.shipment.dto.response.ShipmentResponse;
import com.om.smartpost.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final AuthenticatedUserFacade authenticatedUserFacade;
    private final StaffManagementService staffManagementService;

    @PostMapping("/create")
    public ResponseEntity<ShipmentResponse> createShipment(Authentication authentication, @Valid @RequestBody ShipmentCreateRequest request) {
        return ResponseEntity.ok(shipmentService.createShipment(authenticatedUserFacade.currentUser(authentication), request));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> trackShipment(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getShipmentByTrackingNumber(trackingNumber));
    }

    @GetMapping("/office")
    @PreAuthorize("hasAnyRole('POSTADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<ShipmentResponse>> getOfficeShipments(Authentication authentication) {
        PostAdminProfileResponse profile = staffManagementService.getAdminProfile(authenticatedUserFacade.currentUserId(authentication));
        return ResponseEntity.ok(shipmentService.getShipmentsForOffice(profile.officePincode()));
    }

    @GetMapping("/my-shipments")
    public ResponseEntity<List<ShipmentResponse>> getMyShipments(Authentication authentication) {
        return ResponseEntity.ok(shipmentService.getMyShipments(authenticatedUserFacade.currentUserId(authentication)));
    }

    @GetMapping("/all-shipments")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments(Authentication authentication) {
        var user = authenticatedUserFacade.currentUser(authentication);
        return ResponseEntity.ok(shipmentService.getAllMyShipments(user.getUserId(), user.getMobileNo(), user.getEmail()));
    }

    @PutMapping("/{trackingNumber}/assign-postman")
    public ResponseEntity<ShipmentResponse> assignPostman(@PathVariable String trackingNumber, @Valid @RequestBody AssignPostmanRequest request) {
        return ResponseEntity.ok(shipmentService.assignPostman(trackingNumber, request.getEmployeeId()));
    }

    @PutMapping("/{trackingNumber}/status")
    @PreAuthorize("hasRole('POSTADMIN')")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable String trackingNumber,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(
                trackingNumber,
                request.newStatus(),
                request.description(),
                request.location(),
                authenticatedUserFacade.currentUserId(authentication)
        ));
    }
}

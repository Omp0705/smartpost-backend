package com.om.smartpost.shipment.publicapi;

import com.om.smartpost.shipment.dto.response.ShipmentResponse;
import com.om.smartpost.core.notification.MagicLinkService;
import com.om.smartpost.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/public/shipments")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*") // Crucial if testing from different domains
public class PublicShipmentController {

    private final ShipmentService shipmentService;
    private final MagicLinkService magicLinkService;

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> trackShipmentPublic(
            @PathVariable String trackingNumber,
            @RequestParam("token") String token) {

        // 1. Test Token
        verifyTokenOrThrow(trackingNumber, token);

        // 2. Test Database Fetch
        try {
            ShipmentResponse response = shipmentService.getShipmentByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ ERROR FETCHING SHIPMENT FROM DB: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/track/{trackingNumber}/slot")
    public ResponseEntity<ShipmentResponse> updateDeliverySlotPublic(
            @PathVariable String trackingNumber,
            @RequestParam("token") String token,
            @RequestParam("newSlot") String newSlot) {

        verifyTokenOrThrow(trackingNumber, token);

        // Update the slot via service
        ShipmentResponse updated = shipmentService.updatePreferredSlot(trackingNumber, newSlot);
        return ResponseEntity.ok(updated);
    }

    private void verifyTokenOrThrow(String trackingNumber, String token) {
        try {
            String tokenTrackingNumber = magicLinkService.validateTokenAndGetTrackingNumber(token);
            if (!trackingNumber.equals(tokenTrackingNumber)) {
                System.out.println("❌ TOKEN MISMATCH: URL has " + trackingNumber + " but token belongs to " + tokenTrackingNumber);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token mismatch");
            }
            System.out.println("✅ TOKEN IS VALID FOR: " + trackingNumber);
        } catch (Exception e) {
            System.out.println("❌ TOKEN VALIDATION CRASHED!");
            e.printStackTrace(); // <-- THIS IS THE MOST IMPORTANT LINE
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired link");
        }
    }
}




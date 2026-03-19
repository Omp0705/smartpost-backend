package com.om.smartpost.profile.controller;

import com.om.smartpost.core.security.AuthenticatedUserFacade;
import com.om.smartpost.profile.dto.request.AddressCreateRequest;
import com.om.smartpost.profile.dto.request.DeliveryPreferencesUpdateRequest;
import com.om.smartpost.profile.dto.response.DeliveryPreferencesResponse;
import com.om.smartpost.profile.dto.response.SavedAddressResponse;
import com.om.smartpost.profile.dto.response.UserProfileResponse;
import com.om.smartpost.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authenticatedUserFacade.currentUserId(authentication)));
    }

    @PatchMapping("/me/delivery-preference")
    public ResponseEntity<DeliveryPreferencesResponse> updatePreference(
            Authentication authentication,
            @RequestBody DeliveryPreferencesUpdateRequest request
    ) {
        return ResponseEntity.ok(profileService.updateDeliveryPreferences(authenticatedUserFacade.currentUserId(authentication), request));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<SavedAddressResponse> addAddress(
            Authentication authentication,
            @RequestBody AddressCreateRequest request
    ) {
        return ResponseEntity.ok(profileService.addAddress(authenticatedUserFacade.currentUserId(authentication), request));
    }

    @PatchMapping("/me/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            Authentication authentication,
            @RequestBody java.util.Map<String, String> request
    ) {
        profileService.updateFcmToken(authenticatedUserFacade.currentUserId(authentication), request.get("token"));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/addresses/{id}")
    public ResponseEntity<SavedAddressResponse> updateAddress(
            @PathVariable UUID id,
            @RequestBody AddressCreateRequest request) {
        return ResponseEntity.ok(profileService.updateAddress(id, request));
    }

    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID id) {
        profileService.deleteAddress(id);
        return ResponseEntity.ok().build();
    }
}

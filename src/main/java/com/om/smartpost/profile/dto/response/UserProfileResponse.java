package com.om.smartpost.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileResponse {

    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;

    private List<SavedAddressSummaryResponse> addresses;


    private DeliveryPreferencesResponse deliveryPreferences;
}





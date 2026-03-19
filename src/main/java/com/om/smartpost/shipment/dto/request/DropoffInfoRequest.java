package com.om.smartpost.shipment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DropoffInfoRequest(
        @NotBlank(message = "Dropoff office identifier is required")
        String dropoffOffice,
        @NotBlank(message = "Dropoff pincode is required")
        @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits")
        String pincode
) {
}




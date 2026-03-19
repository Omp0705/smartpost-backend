package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.Title;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShipmentContactRequest(
        Title title,
        @NotBlank(message = "First name is required")
        String fname,
        String mName,
        @NotBlank(message = "Last name is required")
        String lName,
        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\d{10,15}$", message = "Phone number must be between 10 and 15 digits")
        String mobileNo,
        @Email(message = "Invalid email format")
        String email,
        String companyName,
        @NotBlank(message = "Address Line 1 is required")
        String addressLine1,
        String addressLine2,
        String landmark,
        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits")
        String pincode,
        @NotBlank(message = "City is required")
        String city,
        @NotBlank(message = "State is required")
        String state,
        String dac,
        Double latitude,
        Double longitude
) {
}




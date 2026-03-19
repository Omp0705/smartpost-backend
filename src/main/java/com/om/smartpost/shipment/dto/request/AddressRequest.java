package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.AddressType;
import com.om.smartpost.shipment.enums.Title;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        AddressType addressType,
        Title title,
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 chars")
        String firstName,
        String middleName,
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 chars")
        String lastName,
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
        String mobileNo,
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
        String state
) {
}





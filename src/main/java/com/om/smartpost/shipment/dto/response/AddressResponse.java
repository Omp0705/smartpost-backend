package com.om.smartpost.shipment.dto.response;

import com.om.smartpost.shipment.enums.AddressType;
import com.om.smartpost.shipment.enums.Title;

public record AddressResponse(
        AddressType addressType,
        Title title,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String mobileNo,
        String companyName,
        String addressLine1,
        String addressLine2,
        String landmark,
        String pincode,
        String city,
        String state
) {
}





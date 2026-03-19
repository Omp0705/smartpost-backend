package com.om.smartpost.shipment.dto.response;

import com.om.smartpost.shipment.enums.Title;

public record ShipmentContactResponse(
        Title title,
        String fName,
        String mName,
        String lName,
        String mobileNo,
        String email,
        String companyName,
        String addressLine1,
        String addressLine2,
        String landmark,
        String pincode,
        String cityDistrict,
        String state,
        String dac,
        Double latitude,
        Double longitude
) {
}




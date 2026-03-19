package com.om.smartpost.profile.dto.request;

import com.om.smartpost.shipment.enums.AddressType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddressCreateRequest {
    private AddressType type;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;
    private Boolean isDefault;
}





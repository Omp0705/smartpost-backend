package com.om.smartpost.profile.dto.response;

import com.om.smartpost.shipment.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class SavedAddressResponse {
    private AddressType type;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;
    private boolean isDefault;
}





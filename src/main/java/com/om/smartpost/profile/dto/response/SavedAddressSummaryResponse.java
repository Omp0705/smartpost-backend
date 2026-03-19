package com.om.smartpost.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.om.smartpost.shipment.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
public class SavedAddressSummaryResponse {
    private UUID id;
    private AddressType type;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;
    @JsonProperty("isDefault")
    private Boolean isDefault;
}





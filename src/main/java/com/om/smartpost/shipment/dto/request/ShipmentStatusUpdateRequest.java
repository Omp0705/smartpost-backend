package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipmentStatusUpdateRequest {
    @NotNull
    private ShipmentStatus status;

    @NotBlank
    private String description;

    @NotBlank
    private String location;
}




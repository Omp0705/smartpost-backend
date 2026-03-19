package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "New status is required")
        ShipmentStatus newStatus,
        String description,
        String location
) {}



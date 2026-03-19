package com.om.smartpost.shipment.dto.response;

import com.om.smartpost.shipment.enums.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentTrackingEventResponse(
        ShipmentStatus status,
        String description,
        String location,
        LocalDateTime timestamp
) {
}




package com.om.smartpost.office.beat.dto.response;

import java.util.UUID;

public record BeatResponse(
        UUID id,
        UUID officeId,
        String officeName,
        String beatCode,
        String name,
        String description,
        String areaKeywords,
        Integer routeOrder,
        Boolean active,
        UUID assignedPostmanId,
        String assignedPostmanEmployeeId,
        String assignedPostmanName,
        int shipmentCount
) {
}




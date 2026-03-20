package com.om.smartpost.office.postoffice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostOfficeResponse(
        UUID id,
        String name,
        String description,
        String branchType,
        String deliveryStatus,
        String circle,
        String district,
        String division,
        String region,
        String block,
        String state,
        String country,
        String pincode,
        String address,
        Double lat,
        Double lng,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}




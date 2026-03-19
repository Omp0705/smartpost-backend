package com.om.smartpost.office.staff.dto.response;

import java.util.List;
import java.util.UUID;

public record PostmanResponse(
        UUID id,
        Long userId,
        String fullName,
        String username,
        String email,
        String mobileNo,
        Boolean active,
        String employeeId,
        String vehicleNumber,
        UUID officeId,
        String officeName,
        List<UUID> beatIds,
        List<String> beatNames
) {
}




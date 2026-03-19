package com.om.smartpost.office.staff.dto.response;

import java.util.UUID;

public record PostAdminResponse(
        UUID id,
        Long userId,
        String fullName,
        String username,
        String email,
        String mobileNo,
        Boolean active,
        String employeeId,
        String designation,
        UUID officeId,
        String officeName,
        String officePincode,
        boolean isDeliveryOffice
) {
}




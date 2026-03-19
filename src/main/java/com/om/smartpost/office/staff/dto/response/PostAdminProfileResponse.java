package com.om.smartpost.office.staff.dto.response;

import java.util.UUID;

public record PostAdminProfileResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email,
        String mobileNo,
        String designation,

        // Office Details needed for UI logic and Data filtering
        UUID officeId,
        String officeName,
        String officePincode,
        boolean isDeliveryOffice
) {}



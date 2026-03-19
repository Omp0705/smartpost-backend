package com.om.smartpost.shipment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignPostmanRequest {
    @NotBlank
    private String employeeId;
}




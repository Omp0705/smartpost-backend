package com.om.smartpost.office.beat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BeatRequest {

    @NotNull(message = "Office ID is required")
    private UUID officeId;

    @NotBlank(message = "Beat code is required")
    private String beatCode;

    @NotBlank(message = "Beat name is required")
    private String name;

    private String description;

    private String areaKeywords;

    private Integer routeOrder;

    private Boolean active;
}



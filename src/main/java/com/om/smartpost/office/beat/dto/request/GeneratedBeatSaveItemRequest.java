package com.om.smartpost.office.beat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GeneratedBeatSaveItemRequest {

    @NotBlank(message = "Beat code is required")
    private String beatCode;

    @NotBlank(message = "Beat name is required")
    private String name;

    private String description;

    private String areaKeywords;

    private Integer routeOrder;

    private UUID assignedPostmanId;

    @NotEmpty(message = "Shipment IDs are required")
    private List<UUID> shipmentIds;
}

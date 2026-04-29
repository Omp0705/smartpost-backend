package com.om.smartpost.office.beat.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GeneratedBeatSaveRequest {

    @NotNull(message = "Office ID is required")
    private UUID officeId;

    @Valid
    @NotEmpty(message = "At least one generated beat is required")
    private List<GeneratedBeatSaveItemRequest> beats;
}

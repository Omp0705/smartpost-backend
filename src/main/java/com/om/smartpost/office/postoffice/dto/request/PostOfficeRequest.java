package com.om.smartpost.office.postoffice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostOfficeRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String branchType;

    @NotBlank
    private String deliveryStatus;

    @NotBlank
    private String circle;

    @NotBlank
    private String district;

    @NotBlank
    private String division;

    @NotBlank
    private String region;

    private String block;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @NotBlank
    private String pincode;
}




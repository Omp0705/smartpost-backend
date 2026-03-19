package com.om.smartpost.office.staff.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PostAdminCreateRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @Email
    private String email;

    @Size(min = 6)
    private String mobileNo;

    @NotBlank
    @Size(min = 6)
    private String password;

    private String designation;

    @NotNull
    private UUID officeId;
}




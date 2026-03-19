package com.om.smartpost.office.staff.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PostAdminUpdateRequest {

    private String fullName;
    private String username;

    @Email
    private String email;

    @Size(min = 6)
    private String mobileNo;

    @Size(min = 6)
    private String password;

    private String employeeId;
    private String designation;
    private UUID officeId;
    private Boolean active;
}




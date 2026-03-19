package com.om.smartpost.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordRequest {
    // Getters and Setters
    private String email;
    private String newPassword;

}



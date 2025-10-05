package com.om.smartpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {

//    (username | email)
    @NotBlank(message = "Username or email required")
    private String identifier;

    @NotBlank(message = "Password required")
    private String password;

}

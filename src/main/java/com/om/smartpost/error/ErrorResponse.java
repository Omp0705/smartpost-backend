package com.om.smartpost.error;

import lombok.Data;

import java.util.Map;

@Data

public class ErrorResponse {
    private String code;
    private String message;

    // default ctor for Jackson
    public ErrorResponse() {}

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }


}
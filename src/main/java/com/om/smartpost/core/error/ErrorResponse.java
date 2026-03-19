package com.om.smartpost.core.error;

import lombok.Data;

import java.util.Map;

@Data

public class ErrorResponse {
    private String code;
    private String message;
    private Map<String, String> fieldErrors;

    // default ctor for Jackson
    public ErrorResponse() {}

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

}




package com.om.smartpost.core.exception;


import lombok.Getter;


//  Custom Exception for Duplicate Resource
@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String code;

    public DuplicateResourceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DuplicateResourceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}




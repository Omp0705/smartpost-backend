package com.om.smartpost.core.exception;

import com.om.smartpost.core.error.ErrorCodes;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ConstraintViolationTranslator {

    private static final Map<String, ErrorCodes> CONSTRAINT_TO_CODE = new HashMap<>();

    static {
        // Adjust these keys to your actual DB constraint names (recommended to name them in migrations)
        CONSTRAINT_TO_CODE.put("uq_user_username", ErrorCodes.USERNAME_EXISTS);
        CONSTRAINT_TO_CODE.put("uq_user_email", ErrorCodes.EMAIL_EXISTS);

        // Common hibernate/postgres default names (if you didn't explicitly name them)
        CONSTRAINT_TO_CODE.put("users_username_key", ErrorCodes.USERNAME_EXISTS);
        CONSTRAINT_TO_CODE.put("users_email_key", ErrorCodes.EMAIL_EXISTS);
    }

    private ConstraintViolationTranslator() {}

    public static Optional<ErrorCodes> toErrorCode(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) cause;
            String constraintName = cve.getConstraintName();
            if (constraintName != null && CONSTRAINT_TO_CODE.containsKey(constraintName)) {
                return Optional.of(CONSTRAINT_TO_CODE.get(constraintName));
            }
        }

        // fallback: inspect message (less reliable)
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("username") && lower.contains("unique")) {
                return Optional.of(ErrorCodes.USERNAME_EXISTS);
            }
            if (lower.contains("email") && lower.contains("unique")) {
                return Optional.of(ErrorCodes.EMAIL_EXISTS);
            }
        }
        return Optional.empty();
    }
}



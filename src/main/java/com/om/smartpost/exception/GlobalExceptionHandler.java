package com.om.smartpost.exception;

import com.om.smartpost.error.ErrorCodes;
import com.om.smartpost.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        String code = ex.getCode() != null ? ex.getCode() : ErrorCodes.DUPLICATE_RESOURCE.toString();
        String message = ex.getMessage() != null ? ex.getMessage() : "Resource conflict";
        return buildResponse(HttpStatus.CONFLICT, code, message, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        // Attempt to map DB constraint -> code
        ErrorCodes code = ConstraintViolationTranslator.toErrorCode(ex).orElse(ErrorCodes.DUPLICATE_RESOURCE);
        String message = switch (code) {
            case USERNAME_EXISTS -> "Username already exists";
            case EMAIL_EXISTS -> "Email already exists";
            case MOBILE_EXISTS -> "Mobile No. already exists";
            default -> "Resource already exists";
        };
        return buildResponse(HttpStatus.CONFLICT, code.toString(), message, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodes.VALIDATION_FAILED.toString(),
                "One or more fields are invalid", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        cv -> {
                            String path = cv.getPropertyPath().toString();
                            String[] parts = path.split("\\.");
                            return parts[parts.length - 1];
                        },
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodes.VALIDATION_FAILED.toString(),
                "One or more fields are invalid", fieldErrors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS.toString(),
                "Username or password is incorrect", null);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ErrorCodes.USER_NOT_FOUND.toString(),
                ex.getMessage() != null ? ex.getMessage() : "User not found", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid input", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR.toString(),
                "Something went wrong. Try again later.", null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message, Map<String, String> fieldErrors) {
        ErrorResponse err = new ErrorResponse(code, message);
        return ResponseEntity.status(status).body(err);
    }
}

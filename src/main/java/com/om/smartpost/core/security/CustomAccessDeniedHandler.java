package com.om.smartpost.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.resetBuffer();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ErrorResponse body = new ErrorResponse(
                ErrorCodes.FORBIDDEN.toString(),
                "You do not have permission to access this resource"
        );
        new ObjectMapper().writeValue(response.getOutputStream(), body);

        // Ensure everything is written and response is finished
        response.flushBuffer();
    }
}


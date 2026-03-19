package com.om.smartpost.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorResponse body = new ErrorResponse(
                ErrorCodes.UNAUTHENTICATED.toString(),
                "Authentication is required to access this resource"
        );
        new ObjectMapper().writeValue(response.getOutputStream(), body);

    }
}


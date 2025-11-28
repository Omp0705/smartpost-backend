package com.om.smartpost.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response); // continue request flow
        long timeTaken = System.currentTimeMillis() - start;

        int status = response.getStatus(); // <-- Response status code

        logger.info(
                "REQUEST: [" + request.getMethod() + " " + request.getRequestURI() + "] " +
                        "STATUS: " + status +
                        " TIME: " + timeTaken + "ms"
        );
    }
}

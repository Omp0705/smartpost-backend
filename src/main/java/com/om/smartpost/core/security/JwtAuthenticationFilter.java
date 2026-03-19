package com.om.smartpost.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.error.ErrorResponse;
import com.om.smartpost.core.identity.CustomUserDetailsService;
import com.om.smartpost.core.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            // Extract token from header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token);
            }

            // validate the token and set the authentication context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e){
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHENTICATED.toString(), "Token expired");
        }catch (SignatureException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHENTICATED.toString(), "Invalid token signature");
        }
        catch (Exception e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHENTICATED.toString(), "Authentication error");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Tell the filter to IGNORE the public tracking URLs
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/api/v1/public/") ||
                path.equals("/tracking.html");
    }

    //Helper method's
    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        new ObjectMapper().writeValue(response.getOutputStream(), new ErrorResponse(code, message));
    }
}


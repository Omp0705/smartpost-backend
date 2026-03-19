package com.om.smartpost.core.notification;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class MagicLinkService {

    @Value("${app.magic-link.secret}")
    private String jwtSecret;

    @Value("${app.magic-link.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generates an 8-hour token tied strictly to a tracking number
    public String generateTrackingToken(String trackingNumber) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(trackingNumber)
                .claim("purpose", "TRACK_AND_UPDATE")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validates the token and extracts the tracking number
    public String validateTokenAndGetTrackingNumber(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"TRACK_AND_UPDATE".equals(claims.get("purpose"))) {
            throw new RuntimeException("Invalid token purpose");
        }

        return claims.getSubject();
    }
}

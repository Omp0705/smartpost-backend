package com.om.smartpost.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartpost.geocoding")
public record GeocodingProperties(
        String baseUrl,
        String userAgent,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}





package com.om.smartpost.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartpost.prediction")
public record PredictionProperties(
        String baseUrl,
        String predictPath,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}




package com.om.smartpost.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({PredictionProperties.class, GeocodingProperties.class})
public class PredictionConfig {

    @Bean
    RestClient predictionRestClient(RestClient.Builder builder, PredictionProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMs());
        requestFactory.setReadTimeout(properties.readTimeoutMs());
        return builder
                .requestFactory(requestFactory)
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Bean
    RestClient geocodingRestClient(RestClient.Builder builder, GeocodingProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMs());
        requestFactory.setReadTimeout(properties.readTimeoutMs());
        return builder
                .requestFactory(requestFactory)
                .baseUrl(properties.baseUrl())
                .defaultHeader("User-Agent", properties.userAgent())
                .build();
    }
}




package com.om.smartpost.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true); // Shows the JSON body
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false); // Set true if you need to see JWT headers
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
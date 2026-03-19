package com.om.smartpost;

import com.om.smartpost.core.config.SuperAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(SuperAdminProperties.class)
@EnableAsync
public class SmartPostApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartPostApplication.class, args);
	}

}


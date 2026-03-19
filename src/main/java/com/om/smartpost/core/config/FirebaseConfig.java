package com.om.smartpost.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        // Using the path from your application.properties for flexibility
        try (InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(firebaseConfigPath)) {

            if (serviceAccount == null) {
                log.error("Firebase config file not found at path: {}", firebaseConfigPath);
                return;
            }

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount)) // Added missing )
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Application has been successfully initialized.");
            } else {
                log.info("Firebase Application already initialized.");
            }
        } catch (IOException e) {
            log.error("Firebase initialization error: {}", e.getMessage(), e);
        }
    }
}



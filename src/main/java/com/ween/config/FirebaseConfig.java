package com.ween.config;

// Firebase Configuration (DISABLED - Firebase dependency commented out)
/*
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${ween.firebase.credentials-path}")
    private String credentialsPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsPath)
            );
            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();
            FirebaseApp.initializeApp(firebaseOptions);
            log.info("Firebase initialized");
        }
        return FirebaseMessaging.getInstance();
    }
}
*/

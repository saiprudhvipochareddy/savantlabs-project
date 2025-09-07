package com.savantlabs.adapters.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class FirestoreConfig {

    @Bean
    public Firestore firestore() throws Exception {
        String credPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credPath == null) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS not set");
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credPath));
        return FirestoreOptions.newBuilder()
                .setCredentials(credentials)
                .build().getService();
    }
}
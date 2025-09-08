package com.savantlabs.adapters.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.util.Objects;

@Configuration
public class FirestoreConfig {
    private final String credentialPath;

    public FirestoreConfig(@Value("${firestore.credentials}") String credentialPath) {
        this.credentialPath = credentialPath;
    }

    @Bean
    public Firestore firestore() throws Exception {
        if (Objects.isNull(credentialPath)) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS not set");
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialPath));
        return FirestoreOptions.newBuilder()
                .setCredentials(credentials)
                .build().getService();
    }
}
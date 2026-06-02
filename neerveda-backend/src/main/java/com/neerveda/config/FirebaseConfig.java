package com.neerveda.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * 🔥 FirebaseConfig
 *
 * Initializes Firebase synchronously in a @Bean method (not @PostConstruct)
 * so Spring can enforce correct bean creation order via @DependsOn.
 *
 * Credentials loaded from (in priority order):
 *  1. GOOGLE_APPLICATION_CREDENTIALS_JSON env variable (Render / production)
 *  2. firebase-service-account.json in classpath (local dev)
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-file:firebase-service-account.json}")
    private String credentialsFile;

    @Value("${firebase.project-id:neerveda}")
    private String projectId;

    /**
     * Initializes Firebase AND returns the FirebaseApp in one bean.
     * All other beans that need Firebase should @DependsOn("firebaseApp").
     */
    @Bean("firebaseApp")
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("🔥 Firebase already initialized.");
            return FirebaseApp.getInstance();
        }

        GoogleCredentials credentials = loadCredentials();
        if (credentials == null) {
            log.error("❌ No Firebase credentials found. Set GOOGLE_APPLICATION_CREDENTIALS_JSON env variable on Render.");
            throw new IllegalStateException(
                "Firebase credentials not found. " +
                "Set GOOGLE_APPLICATION_CREDENTIALS_JSON environment variable."
            );
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("🔥 Firebase connected successfully! Project: {}", projectId);
        return app;
    }

    /**
     * Firestore bean — depends on firebaseApp being initialized first.
     */
    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    // -------------------------------------------------------
    // CREDENTIAL LOADING
    // -------------------------------------------------------

    private GoogleCredentials loadCredentials() throws IOException {
        // Option 1: JSON content as environment variable (Render, Docker, CI)
        String credJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (credJson != null && !credJson.isBlank()) {
            log.info("🔥 Loading Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS_JSON env var.");
            try (var stream = new java.io.ByteArrayInputStream(credJson.getBytes())) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        // Option 2: classpath file (local development)
        try {
            ClassPathResource resource = new ClassPathResource(credentialsFile);
            if (resource.exists()) {
                log.info("🔥 Loading Firebase credentials from classpath: {}", credentialsFile);
                try (InputStream stream = resource.getInputStream()) {
                    return GoogleCredentials.fromStream(stream);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load Firebase credentials from classpath: {}", e.getMessage());
        }

        return null;
    }
}

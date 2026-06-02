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
 * Firebase is initialized as optional — if credentials are missing
 * the app starts anyway and logs a warning. This prevents deployment
 * failures when the env variable hasn't been set yet.
 *
 * Credentials loaded from (priority order):
 *  1. GOOGLE_APPLICATION_CREDENTIALS_JSON env variable (Render/production)
 *  2. firebase-service-account.json in classpath (local dev)
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-file:firebase-service-account.json}")
    private String credentialsFile;

    @Value("${firebase.project-id:neerveda}")
    private String projectId;

    @Bean("firebaseApp")
    public FirebaseApp firebaseApp() {
        // Already initialized (e.g. hot reload)
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("🔥 Firebase already initialized.");
            return FirebaseApp.getInstance();
        }

        try {
            GoogleCredentials credentials = loadCredentials();
            if (credentials == null) {
                log.warn("⚠️  GOOGLE_APPLICATION_CREDENTIALS_JSON not set and no classpath file found.");
                log.warn("⚠️  Starting without Firebase — data will NOT persist to Firestore.");
                log.warn("⚠️  Set GOOGLE_APPLICATION_CREDENTIALS_JSON on Render to enable persistence.");
                return null; // graceful degradation
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("🔥 Firebase connected! Project: {}", projectId);
            return app;

        } catch (Exception e) {
            log.error("❌ Firebase init failed: {}", e.getMessage());
            log.warn("⚠️  Starting without Firebase.");
            return null;
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("⚠️  Firestore unavailable — Firebase not initialized.");
            return null;
        }
        return FirestoreClient.getFirestore(firebaseApp);
    }

    private GoogleCredentials loadCredentials() throws IOException {
        // Option 1: env variable (Render production)
        String credJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (credJson != null && !credJson.isBlank()) {
            log.info("🔥 Loading Firebase credentials from env variable.");
            try (var stream = new java.io.ByteArrayInputStream(credJson.getBytes())) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        // Option 2: classpath file (local dev)
        try {
            ClassPathResource resource = new ClassPathResource(credentialsFile);
            if (resource.exists()) {
                log.info("🔥 Loading Firebase credentials from classpath: {}", credentialsFile);
                try (InputStream stream = resource.getInputStream()) {
                    return GoogleCredentials.fromStream(stream);
                }
            }
        } catch (Exception e) {
            log.warn("Classpath credentials not found: {}", e.getMessage());
        }

        return null;
    }
}

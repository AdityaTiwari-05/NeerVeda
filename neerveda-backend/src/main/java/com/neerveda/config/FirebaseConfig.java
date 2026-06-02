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
 * 🔥 FirebaseConfig — Firebase is fully optional.
 *
 * If credentials are missing the app boots normally and returns
 * empty results from all Firestore calls rather than crashing.
 *
 * To enable Firestore on Render:
 *   Set env variable GOOGLE_APPLICATION_CREDENTIALS_JSON = <contents of firebase-service-account.json>
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-file:firebase-service-account.json}")
    private String credentialsFile;

    @Value("${firebase.project-id:neerveda}")
    private String projectId;

    // -------------------------------------------------------
    // Firebase App — returns null gracefully if no credentials
    // @Bean with null return + @Autowired(required=false) downstream
    // -------------------------------------------------------

    @Bean("firebaseApp")
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try {
            GoogleCredentials credentials = loadCredentials();
            if (credentials == null) {
                log.warn("⚠️  No Firebase credentials found — Firestore disabled.");
                log.warn("    Set GOOGLE_APPLICATION_CREDENTIALS_JSON env variable on Render to enable.");
                return null;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("🔥 Firebase connected! Project: {}", projectId);
            return app;
        } catch (Exception e) {
            log.error("❌ Firebase init error: {} — starting without Firestore.", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------
    // Firestore — only created when FirebaseApp is available
    // Uses @Autowired(required=false) via method parameter trick:
    // Spring skips this bean entirely when firebaseApp is null
    // by using @Bean with a conditional check inside.
    // -------------------------------------------------------

    @Bean
    public Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("⚠️  Firestore bean not created — Firebase not initialized.");
            return null;
        }
        try {
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            log.error("❌ Could not create Firestore bean: {}", e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------
    // Credential loading
    // -------------------------------------------------------

    private GoogleCredentials loadCredentials() throws IOException {
        // Priority 1: env variable (Render / Docker / CI)
        String credJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (credJson != null && !credJson.isBlank()) {
            log.info("🔥 Loading Firebase credentials from env variable.");
            try (var stream = new java.io.ByteArrayInputStream(credJson.getBytes())) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        // Priority 2: classpath file (local dev)
        try {
            ClassPathResource resource = new ClassPathResource(credentialsFile);
            if (resource.exists()) {
                log.info("🔥 Loading Firebase credentials from classpath: {}", credentialsFile);
                try (InputStream stream = resource.getInputStream()) {
                    return GoogleCredentials.fromStream(stream);
                }
            }
        } catch (Exception e) {
            log.debug("Classpath credentials not found: {}", e.getMessage());
        }

        return null;
    }
}

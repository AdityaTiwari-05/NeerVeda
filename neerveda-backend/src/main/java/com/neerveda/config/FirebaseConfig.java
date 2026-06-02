package com.neerveda.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
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
 * Initializes Firebase Admin SDK and exposes a Firestore bean.
 *
 * SETUP:
 *  1. Firebase Console → Project Settings → Service Accounts
 *  2. Generate New Private Key → rename to firebase-service-account.json
 *  3. Place in src/main/resources/
 *  4. Set FIREBASE_PROJECT_ID env variable (or update application.properties)
 *
 * ⚠️  NEVER commit firebase-service-account.json to source control.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-file}")
    private String credentialsFile;

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = loadCredentials();
                if (credentials == null) {
                    log.warn("⚠️  No Firebase credentials found — running without Firebase.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

                FirebaseApp.initializeApp(options);
                log.info("🔥 Firebase connected successfully! Project: {}", projectId);
            } else {
                log.info("🔥 Firebase already initialized.");
            }
        } catch (IOException e) {
            log.error("❌ Firebase initialization failed: {}", e.getMessage());
            log.warn("⚠️  Running without Firebase.");
        }
    }

    /**
     * Loads Firebase credentials from:
     * 1. GOOGLE_APPLICATION_CREDENTIALS_JSON env variable (production / Render)
     * 2. firebase-service-account.json file in classpath (local dev)
     */
    private GoogleCredentials loadCredentials() throws IOException {
        // Option 1: credentials JSON as environment variable (Render, Docker)
        String credentialsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            log.info("🔥 Loading Firebase credentials from environment variable.");
            try (var stream = new java.io.ByteArrayInputStream(credentialsJson.getBytes())) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        // Option 2: classpath file (local development)
        try {
            InputStream serviceAccount = new ClassPathResource(credentialsFile).getInputStream();
            log.info("🔥 Loading Firebase credentials from classpath file.");
            return GoogleCredentials.fromStream(serviceAccount);
        } catch (Exception e) {
            log.warn("Firebase service account file not found: {}", credentialsFile);
            return null;
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}

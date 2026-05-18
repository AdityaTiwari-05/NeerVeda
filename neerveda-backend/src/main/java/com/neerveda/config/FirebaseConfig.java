package com.neerveda.config;

import org.springframework.context.annotation.Configuration;

/**
 * 🔥 FirebaseConfig
 *
 * This class will connect NeerVeda to Firebase Firestore database.
 *
 * SETUP STEPS (do this after getting Firebase service account key):
 *
 * 1. Go to Firebase Console → Project Settings → Service Accounts
 * 2. Click "Generate New Private Key" → download JSON file
 * 3. Rename it to "firebase-service-account.json"
 * 4. Place it in: src/main/resources/firebase-service-account.json
 * 5. Uncomment the code below
 *
 * FOR NOW: We are using in-memory storage (ArrayList in controllers)
 * NEXT PHASE: We will connect to Firebase and store data permanently.
 *
 * ⚠️ IMPORTANT: NEVER commit firebase-service-account.json to GitHub!
 *    Add it to .gitignore
 */
@Configuration
public class FirebaseConfig {

    // Firebase initialization will go here in Phase 2
    // Uncomment and complete after downloading service account key:

    /*
    @Value("${firebase.credentials-file}")
    private String credentialsFile;

    @Value("${firebase.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = new ClassPathResource(credentialsFile).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("🔥 Firebase connected successfully!");
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
    */
}

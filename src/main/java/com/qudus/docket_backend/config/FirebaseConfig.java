package com.qudus.docket_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    private final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    private final Environment env;

    public FirebaseConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public FirebaseApp initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized, returning existing instance.");
                return FirebaseApp.getInstance();
            }
            InputStream serviceAccount = getServiceAccountStream();

            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId("docket-bd33b").build();
            log.info("Firebase initialized successfully");
            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Error initializing firebase with message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    private InputStream getServiceAccountStream() {
        InputStream stream;
        try {
            if(env.acceptsProfiles(Profiles.of("prod"))) {
                File renderFile = new File("/etc/secrets/serviceAccountKey.json");
                stream = new FileInputStream(renderFile);
                log.info("Running service account from render file");
            } else {
                stream = new ClassPathResource("serviceAccountKey.json").getInputStream();
                log.info("Running service account from local service account");
            }
        } catch (IOException e) {
            log.error("Failed getting service account with error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return stream;
    }
}

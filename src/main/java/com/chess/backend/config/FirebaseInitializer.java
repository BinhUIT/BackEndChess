package com.chess.backend.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Component
public class FirebaseInitializer {
    @PostConstruct
    public void initialize() {
        try {
            File jsonFile = new File("E:/JAVA_CHESS/BackEndChess/chess-mobile-ae140-firebase-adminsdk-fbsvc-b839b0d449.json");
            InputStream serviceAccount = new FileInputStream(jsonFile); 
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase has been initialized.");
            }

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to initialize Firebase", e);
        }
    
        
    }
    
}

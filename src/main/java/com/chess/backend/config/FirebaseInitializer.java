package com.chess.backend.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseInitializer {
    @PostConstruct
    public void initialize() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("D:\\BackEndChess\\backend\\BackEndChess\\chess-mobile-ae140-firebase-adminsdk-fbsvc-b839b0d449.json");
        FirebaseOptions firebaseOptions= new FirebaseOptions.Builder()
                                            .setCredentials(GoogleCredentials.fromStream(fileInputStream)).build();
        FirebaseApp.initializeApp(firebaseOptions);
    }

}

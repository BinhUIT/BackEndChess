package com.chess.backend.service;

import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@Service
public class FirebaseAuthService { 
    public String getUidFromToken(String idToken) throws FirebaseAuthException  {
        
            // Verify the token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // Get the UID of the user
            String uid = decodedToken.getUid();
            return uid;
        
    }

}

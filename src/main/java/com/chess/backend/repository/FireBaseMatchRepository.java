package com.chess.backend.repository;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

import com.chess.backend.model.Match;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

@Repository
public class FireBaseMatchRepository implements MatchRepository {
    private Firestore dbFirestore;
    private final String COLLECTION_NAME="Match";

    public FireBaseMatchRepository(){
        this.dbFirestore=FirestoreClient.getFirestore();
    }

    @Override
    public void saveMatch (Match match){
        DocumentReference addedDocRef = dbFirestore.collection(COLLECTION_NAME).document(match.getMatchId());
        ApiFuture<WriteResult> writeResult = addedDocRef.set(match);
        try {
            writeResult.get(); // Wait for the operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save match", e);
        }
    }
    
}

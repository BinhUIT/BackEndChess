package com.chess.backend.repository;

import com.chess.backend.model.Player;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.ExecutionException;

public class FireBasePlayerRepository implements PlayerRepository {
    private static final String COLLECTION_NAME = "User";

    @Override
    public Player save(Player player) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference addedDocRef = dbFirestore.collection(COLLECTION_NAME).document(player.getId());
        ApiFuture<WriteResult> writeResult = addedDocRef.set(player);
        try {
            writeResult.get(); // Wait for the operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save player", e);
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(player.getId());
        ApiFuture<WriteResult> writeResult = documentReference.set(player);
        try {
            writeResult.get(); // Wait for the operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update player", e);
        }
        return player;
    }

    @Override
    public void deleteById(String id) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COLLECTION_NAME).document(id).delete();
        try {
            writeResult.get(); // Wait for the operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete player", e);
        }
    }
}

package com.chess.backend.repository;

import com.chess.backend.model.Player;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class FireBasePlayerRepository implements PlayerRepository {
    private static final String COLLECTION_NAME = "User";
    private Firestore dbFirestore;

    public FireBasePlayerRepository (){
        this.dbFirestore=FirestoreClient.getFirestore();
    }

    @Override
    public Player savePlayer(Player player) {
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
    public Player updatePlayer(Player player) {
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
    public void deleteByPlayerId(String id) {
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COLLECTION_NAME).document(id).delete();
        try {
            writeResult.get(); // Wait for the operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete player", e);
        }
    }
    @Override
    public List<Player> getAllPlayers() {
        try {
            ApiFuture<QuerySnapshot> query = dbFirestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            
            return documents.stream()
                    .map(document -> document.toObject(Player.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get players", e);
        }
    }
}

package com.chess.backend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@Component
public class GetDataService {
    @Autowired
    private Firestore fireStore;

    public DocumentSnapshot GetDataSnapShot(String collectionName, String documentName)
            throws InterruptedException, ExecutionException {
        CollectionReference collection = fireStore.collection(collectionName);
        DocumentReference ref = collection.document(documentName);
        ApiFuture<DocumentSnapshot> futureSnap = ref.get();
        DocumentSnapshot snap = futureSnap.get();
        return snap;
    }

    public List<QueryDocumentSnapshot> GetAllDocumentSnapshot(String collectionName)
            throws InterruptedException, ExecutionException {
        CollectionReference collection = fireStore.collection(collectionName);
        ApiFuture<QuerySnapshot> queries = collection.get();
        List<QueryDocumentSnapshot> documents = queries.get().getDocuments();
        return documents;
    }

    public void SetData(String collectionName, String documentName, Map<String, Object> data) {
        DocumentReference docRef = fireStore.collection(collectionName).document(documentName);
        docRef.set(data);
    }

    public void updateAllUserRanks() throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> users = GetAllDocumentSnapshot("User");

        // Sắp xếp giảm dần theo "score"
        users.sort((u1, u2) -> {
            Long score1 = u1.contains("score") ? u1.getLong("score") : 0L;
            Long score2 = u2.contains("score") ? u2.getLong("score") : 0L;
            return Long.compare(score2, score1); // score giảm dần
        });

        // Cập nhật rank cho từng user
        for (int i = 0; i < users.size(); i++) {
            QueryDocumentSnapshot user = users.get(i);
            int rank = i + 1;
            String userId = user.getId(); // document ID
            Map<String, Object> data = Map.of("rank", rank);
            fireStore.collection("User").document(userId).update(data);
        }
    }

}

package com.chess.backend.service;

import java.util.List;
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
    public DocumentSnapshot GetDataSnapShot(String collectionName, String documentName) throws InterruptedException, ExecutionException {
        CollectionReference collection = fireStore.collection(collectionName);
        DocumentReference ref= collection.document(documentName);
        ApiFuture<DocumentSnapshot> futureSnap = ref.get();
        DocumentSnapshot snap = futureSnap.get();
        return snap;
    }
    public List<QueryDocumentSnapshot> GetAllDocumentSnapshot(String collectionName) throws InterruptedException, ExecutionException {
        CollectionReference collection = fireStore.collection(collectionName); 
        ApiFuture<QuerySnapshot> queries = collection.get(); 
        List<QueryDocumentSnapshot> documents = queries.get().getDocuments();
        return documents;
    }
}

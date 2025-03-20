package com.chess.backend.service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

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

}

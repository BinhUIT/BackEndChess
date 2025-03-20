package com.chess.backend.service;

import java.lang.annotation.Documented;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Player;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
@Service
public class PlayerService {
    @Autowired 
    private Firestore firestore;
    @Autowired
    private GetDataService getDataService;
    public Player GetPlayerById(String playerId) throws InterruptedException, ExecutionException {
        DocumentSnapshot snap = getDataService.GetDataSnapShot("User", playerId); 
        if(snap.exists()) {
            System.out.println("Yes");
            return snap.toObject(Player.class);
        } 
        return null;
    }
}

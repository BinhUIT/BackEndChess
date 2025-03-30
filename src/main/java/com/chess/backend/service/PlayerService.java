package com.chess.backend.service;

import java.lang.annotation.Documented;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Player;
import com.chess.backend.request.PlayerRegisterRequest;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
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

    public Player RegisterPlayer(PlayerRegisterRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
            .setEmail(request.getEmail())
            .setPassword(request.getPassword());
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
            Map<String,Object> data= new HashMap<>();
            data.put("playerId", userRecord.getUid());
            data.put("email", request.getEmail());
            data.put("playerName",request.getUserName());
            data.put("matches",0); 
            data.put("rank",0);
            data.put("win",0);
            data.put("score",0);
            getDataService.SetData("User", userRecord.getUid(), data);
            return new Player(userRecord.getUid(), request.getEmail(), request.getUserName(),0,0,0,0);
    }

   
}

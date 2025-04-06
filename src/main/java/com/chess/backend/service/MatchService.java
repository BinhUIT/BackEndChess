package com.chess.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.referencemodel.MatchReferenceModel;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
@Service
public class MatchService {
    @Autowired
    private Firestore firestore;
    @Autowired
    private GetDataService getDataService;
    public List<Match> listMatch() throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> listSnapshots = getDataService.GetAllDocumentSnapshot("Match");
        
        List<Match> listMatch = new ArrayList<>();
        FromListQueryDocumentSnapShotToList(listSnapshots, listMatch);
        return listMatch;
        
    }

    public List<Match> GetMatchesOfPlayer(String playerId) throws InterruptedException, ExecutionException {
        List<Match> res = new ArrayList<>();
        DocumentReference playerRef = firestore.collection("User").document(playerId);
        ApiFuture<QuerySnapshot> query= firestore.collection("Match").whereEqualTo("playerWhite", playerRef).get();
        List<QueryDocumentSnapshot> listQuerySnapShot = query.get().getDocuments();
        FromListQueryDocumentSnapShotToList(listQuerySnapShot, res); 
        query = firestore.collection("Match").whereEqualTo("playerBlack",playerRef).get();
        listQuerySnapShot=query.get().getDocuments();
        FromListQueryDocumentSnapShotToList(listQuerySnapShot, res);
        return res;


    } 
    public void FromListQueryDocumentSnapShotToList(List<QueryDocumentSnapshot> listQueryDocumentSnapshots,List<Match> listMatch) throws InterruptedException, ExecutionException {
        for(QueryDocumentSnapshot snap:listQueryDocumentSnapshots) {
            MatchReferenceModel matchRef= snap.toObject(MatchReferenceModel.class);
            Match match = new Match(matchRef);
            DocumentSnapshot playerSnapShot= matchRef.getPlayerWhite().get().get();
            match.setPlayerWhite(playerSnapShot.toObject(Player.class));  
            playerSnapShot= matchRef.getPlayerBlack().get().get();
            match.setPlayerBlack(playerSnapShot.toObject(Player.class)); 
            listMatch.add(match);
        }
    }
}

package com.chess.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.referencemodel.MatchReferenceModel;
import com.chess.backend.service.GetDataService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
@Service
public class MatchService {
    @Autowired
    private Firestore firestore;
    @Autowired
    private GetDataService getDataService;
    public List<Match> listMatch() throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> listSnapshots = getDataService.GetAllDocumentSnapshot("Match");
        List<MatchReferenceModel> listMatchesRef = new ArrayList<>();
        List<Match> listMatch = new ArrayList<>();
        for(QueryDocumentSnapshot snap:listSnapshots) {
            MatchReferenceModel matchRef= snap.toObject(MatchReferenceModel.class);
            Match match = new Match(matchRef);
            listMatchesRef.add(matchRef); 
            DocumentSnapshot playerSnapShot= matchRef.getPlayerWhite().get().get();
            match.setPlayerWhite(playerSnapShot.toObject(Player.class));  
            playerSnapShot= matchRef.getPlayerBlack().get().get();
            match.setPlayerBlack(playerSnapShot.toObject(Player.class)); 
            listMatch.add(match);
        }  
        return listMatch;
        
    }
}

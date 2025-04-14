package com.chess.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.EMatchState;
import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.referencemodel.MatchReferenceModel;
import com.chess.backend.repository.FireBasePlayerRepository;
import com.chess.backend.request.CreateMatchRequest;
import com.google.api.client.util.DateTime;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {
    @Autowired
    private Firestore firestore;
    @Autowired
    private GetDataService getDataService;

    @Autowired
    private final FireBasePlayerRepository playerRepository;

    public List<Match> listMatch() throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> listSnapshots = getDataService.GetAllDocumentSnapshot("Match");

        List<Match> listMatch = new ArrayList<>();
        FromListQueryDocumentSnapShotToList(listSnapshots, listMatch);
        return listMatch;

    }

    public List<Match> GetMatchesOfPlayer(String playerId) throws InterruptedException, ExecutionException {
        List<Match> res = new ArrayList<>();
        DocumentReference playerRef = firestore.collection("User").document(playerId);
        ApiFuture<QuerySnapshot> query = firestore.collection("Match").whereEqualTo("playerWhite", playerRef).get();
        List<QueryDocumentSnapshot> listQuerySnapShot = query.get().getDocuments();
        FromListQueryDocumentSnapShotToList(listQuerySnapShot, res);
        query = firestore.collection("Match").whereEqualTo("playerBlack", playerRef).get();
        listQuerySnapShot = query.get().getDocuments();
        FromListQueryDocumentSnapShotToList(listQuerySnapShot, res);
        return res;

    }

    public void FromListQueryDocumentSnapShotToList(List<QueryDocumentSnapshot> listQueryDocumentSnapshots,
            List<Match> listMatch) throws InterruptedException, ExecutionException {
        for (QueryDocumentSnapshot snap : listQueryDocumentSnapshots) {
            MatchReferenceModel matchRef = snap.toObject(MatchReferenceModel.class);
            Match match = new Match(matchRef);
            DocumentSnapshot playerSnapShot = matchRef.getPlayerWhite().get().get();
            match.setPlayerWhite(playerSnapShot.toObject(Player.class));
            playerSnapShot = matchRef.getPlayerBlack().get().get();
            match.setPlayerBlack(playerSnapShot.toObject(Player.class));
            listMatch.add(match);
        }
    }

    public Match createMatch(CreateMatchRequest request) {
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        String matchId = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("matchId", matchId);
        data.put("matchState", EMatchState.WAITING_FOR_PLAYER);
        data.put("playerWhiteID", request.isPlayAsWhite() ? request.getPlayerID() : null);
        data.put("playerWhiteID", request.isPlayAsWhite() ? null : request.getPlayerID());
        data.put("numberOfTurns", 0);
        data.put("matchTime", new Date());

        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.WAITING_FOR_PLAYER);
        matchReferenceModel.setMatchTime(new Date());
        matchReferenceModel.setNumberOfTurns(0);

        Match match = new Match(matchReferenceModel);

        Player player = playerRepository.findPlayerById(request.getPlayerID());

        if (request.isPlayAsWhite()) {
            match.setPlayerWhite(player);
        } else {
            match.setPlayerBlack(player);
        }
        return match;
    }
    public Match joinMatch(String matchId, String playerId) throws InterruptedException, ExecutionException {
        DocumentSnapshot document = getDataService.GetDataSnapShot("Matches", matchId);
        
        if (!document.exists()) {
            throw new RuntimeException("Match not found");
        }
        
        String matchStateStr = document.getString("matchState");
        EMatchState currentState = EMatchState.valueOf(matchStateStr);
        
        if (currentState != EMatchState.WAITING_FOR_PLAYER) {
            throw new RuntimeException("Cannot join this match");
        }
        
        // Xác định vị trí người chơi (trắng hoặc đen)
        String playerWhiteId = document.getString("playerWhiteId");
        String playerBlackId = document.getString("playerBlackId");
        
        Map<String, Object> updates = new HashMap<>();
        
        if (playerWhiteId == null) {
            updates.put("playerWhiteId", playerId);
            playerWhiteId = playerId;
        } else if (playerBlackId == null) {
            updates.put("playerBlackId", playerId);
            playerBlackId = playerId;
        } else {
            throw new RuntimeException("Match is full");
        }
        
        // Cập nhật trạng thái match
        EMatchState newState = (playerWhiteId != null && playerBlackId != null) 
            ? EMatchState.IN_PROGRESS 
            : EMatchState.WAITING_FOR_PLAYER;
        
        updates.put("matchState", newState.toString());
        
        // Cập nhật dữ liệu trong Firebase
        firestore.collection("Matches").document(matchId).update(updates);
        
        // Tạo và trả về đối tượng Match
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(newState);
        matchReferenceModel.setNumberOfTurns(document.getLong("numberOfTurns").intValue());
        matchReferenceModel.setMatchTime(document.getDate("matchTime"));
        
        Match match = new Match(matchReferenceModel);
        
        // Thêm các người chơi vào match
        if (playerWhiteId != null) {
            match.setPlayerWhite(playerRepository.findPlayerById(playerWhiteId));
        }
        if (playerBlackId != null) {
            match.setPlayerBlack(playerRepository.findPlayerById(playerBlackId));
        }
        
        return match;
    }

    public Match getMatchById(String matchId) throws InterruptedException, ExecutionException {
        // Lấy dữ liệu match từ Firebase
        DocumentSnapshot document = getDataService.GetDataSnapShot("Matches", matchId);
        
        if (!document.exists()) {
            return null;
        }
        
        // Tạo matchReferenceModel từ dữ liệu document
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.valueOf(document.getString("matchState")));
        matchReferenceModel.setNumberOfTurns(document.getLong("numberOfTurns").intValue());
        matchReferenceModel.setMatchTime(document.getDate("matchTime"));
        
        Match match = new Match(matchReferenceModel);
        
        // Thêm thông tin người chơi
        String playerWhiteId = document.getString("playerWhiteId");
        String playerBlackId = document.getString("playerBlackId");
        
        if (playerWhiteId != null) {
            match.setPlayerWhite(playerRepository.findPlayerById(playerWhiteId));
        }
        
        if (playerBlackId != null) {
            match.setPlayerBlack(playerRepository.findPlayerById(playerBlackId));
        }
        
        return match;
    }
}

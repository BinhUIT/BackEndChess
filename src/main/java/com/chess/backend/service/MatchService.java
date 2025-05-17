package com.chess.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chess.backend.model.EMatchState;
import com.chess.backend.model.EMatchType;
import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.GameState;
import com.chess.backend.referencemodel.MatchReferenceModel;
import com.chess.backend.repository.FireBaseMatchRepository;
import com.chess.backend.repository.FireBasePlayerRepository;
import com.chess.backend.repository.MatchRepository;
import com.chess.backend.request.CreateMatchRequest;
import com.chess.backend.request.MoveRequest;
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

    @Autowired
    private final FireBaseMatchRepository fireBaseMatchRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

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

    public Match createPrivateMatch(CreateMatchRequest request) {
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        String matchId = UUID.randomUUID().toString();
        String playerPath = "/User/" + request.getPlayerID();
        // Tao HashMap de luu len FireBase
        Map<String, Object> data = new HashMap<>();
        data.put("matchId", matchId);
        data.put("matchState", EMatchState.WAITING_FOR_PLAYER);
        data.put("playerWhite", request.isPlayAsWhite() ? playerPath : null);
        data.put("playerBlack", request.isPlayAsWhite() ? null : playerPath);
        data.put("matchType", EMatchType.PRIVATE);
        data.put("playTime", request.getPlayTime());
        data.put("numberOfTurns", 0);
        data.put("matchTime", new Date());

        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.WAITING_FOR_PLAYER);
        matchReferenceModel.setMatchType(request.getMatchType());
        matchReferenceModel.setPlayTime(request.getPlayTime());
        matchReferenceModel.setMatchTime(new Date());
        matchReferenceModel.setNumberOfTurns(0);

        Match match = new Match(matchReferenceModel);

        Player player = playerRepository.findPlayerById(request.getPlayerID());

        // set lai playerWhite va playerBlack
        if (request.isPlayAsWhite()) {
            match.setPlayerWhite(player);
        } else {
            match.setPlayerBlack(player);
        }

        firestore.collection("Match").document(matchId).set(data);
        return match;
    }

    public Match createRankedMatch(CreateMatchRequest request, Player opponent) {
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        String matchId = UUID.randomUUID().toString();
        String playerPath = "/User/" + request.getPlayerID();
        //random 0 hoặc 1
        int randomInt=(int) (Math.random()*2);
        // Tao HashMap de luu len FireBase
        Map<String, Object> data = new HashMap<>();
        data.put("matchId", matchId);
        data.put("matchState", EMatchState.WAITING_FOR_PLAYER);
        data.put("playerWhite", randomInt==0? playerPath : null);
        data.put("playerBlack", randomInt==0 ? null : playerPath);
        data.put("matchType", EMatchType.PRIVATE);
        data.put("playTime", request.getPlayTime());
        data.put("numberOfTurns", 0);
        data.put("matchTime", new Date());

        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.WAITING_FOR_PLAYER);
        matchReferenceModel.setMatchType(request.getMatchType());
        matchReferenceModel.setPlayTime(request.getPlayTime());
        matchReferenceModel.setMatchTime(new Date());
        matchReferenceModel.setNumberOfTurns(0);

        Match match = new Match(matchReferenceModel);

        Player player = playerRepository.findPlayerById(request.getPlayerID());

        // set lai playerWhite va playerBlack
        if (request.isPlayAsWhite()) {
            match.setPlayerWhite(player);
        } else {
            match.setPlayerBlack(player);
        }

        firestore.collection("Match").document(matchId).set(data);
        return match;
    }

    public Match joinMatch(String matchId, String playerId) throws InterruptedException, ExecutionException {
        DocumentSnapshot document = getDataService.GetDataSnapShot("Match", matchId);

        if (!document.exists()) {
            throw new RuntimeException("Match not found");
        }

        String matchStateStr = document.getString("matchState");
        EMatchState currentState = EMatchState.valueOf(matchStateStr);

        if (currentState != EMatchState.WAITING_FOR_PLAYER) {
            throw new RuntimeException("Cannot join this match");
        }

        // Xác định vị trí người chơi (trắng hoặc đen)
        String playerWhitePath = document.getString("playerWhite");
        String playerBlackPath = document.getString("playerBlack");

        Map<String, Object> updates = new HashMap<>();

        String playerPath = "/User/" + playerId;

        if (playerWhitePath == null) {
            updates.put("playerWhite", playerPath);
            playerWhitePath = playerPath;
        } else if (playerBlackPath == null) {
            updates.put("playerBlackId", playerPath);
            playerBlackPath = playerPath;
        } else {
            throw new RuntimeException("Match is full");
        }

        // Cập nhật trạng thái match
        EMatchState newState = (playerWhitePath != null && playerBlackPath != null)
                ? EMatchState.IN_PROGRESS
                : EMatchState.WAITING_FOR_PLAYER;

        updates.put("matchState", newState.toString());

        // Cập nhật dữ liệu trong Firebase
        firestore.collection("Match").document(matchId).update(updates);

        // Tạo và trả về đối tượng Match
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(newState);
        matchReferenceModel.setNumberOfTurns(document.getLong("numberOfTurns").intValue());
        matchReferenceModel.setMatchTime(document.getDate("matchTime"));

        Match match = new Match(matchReferenceModel);

        String playerWhiteId = playerWhitePath != null ? extractPlayerIdFromPath(playerWhitePath) : null;
        String playerBlackId = playerBlackPath != null ? extractPlayerIdFromPath(playerBlackPath) : null;

        // Thêm các người chơi vào match
        if (playerWhiteId != null) {
            match.setPlayerWhite(playerRepository.findPlayerById(playerWhiteId));
        }
        if (playerBlackId != null) {
            match.setPlayerBlack(playerRepository.findPlayerById(playerBlackId));
        }

        return match;
    }

    private String extractPlayerIdFromPath(String path) {
        if (path == null)
            return null;
        // Đường dẫn có dạng "/User/{playerId}"
        if (path.startsWith("/User/")) {
            return path.substring(6); // Loại bỏ "/User/"
        }
        return path; // Trả về nguyên bản nếu không có định dạng như mong đợi
    }

    public Match getMatchById(String matchId) throws InterruptedException, ExecutionException {
        // Lấy dữ liệu match từ Firebase
        DocumentSnapshot document = getDataService.GetDataSnapShot("Match", matchId);

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
        String playerWhitePath = document.getString("playerWhite");
        String playerBlackPath = document.getString("playerBlack");
        //Lay playerId tu path
        String playerWhiteId = playerWhitePath != null ? extractPlayerIdFromPath(playerWhitePath) : null;
        String playerBlackId = playerBlackPath != null ? extractPlayerIdFromPath(playerBlackPath) : null;

        if (playerWhiteId != null) {
            match.setPlayerWhite(playerRepository.findPlayerById(playerWhiteId));
        }

        if (playerBlackId != null) {
            match.setPlayerBlack(playerRepository.findPlayerById(playerBlackId));
        }

        return match;
    }

    public void PlayerMove(MoveRequest request) throws InterruptedException, ExecutionException, Exception {
        DocumentSnapshot document;
        try {
            document = getDataService.GetDataSnapShot("Match", request.getCurrentMatchId());
        } catch (InterruptedException | ExecutionException e) {

            throw e;
        }
        if (!document.exists()) {
            throw new Exception("Match not found");
        }
        Match match = document.toObject(Match.class);
        if (match == null) {
            throw new Exception("Err");
        }
        match.setNumberOfTurns(match.getNumberOfTurns() + 1);
        fireBaseMatchRepository.saveMatch(match);
        simpMessagingTemplate.convertAndSend("/topic/match/" + request.getCurrentMatchId(), request.getGameState());
    }

    public void StartGame(int currentMatchId) throws InterruptedException, ExecutionException, Exception {
        DocumentSnapshot document;
        try {
            document = getDataService.GetDataSnapShot("Matches", Integer.toString(currentMatchId));
        } catch (InterruptedException | ExecutionException e) {

            throw e;
        }
        if (!document.exists()) {
            throw new Exception("Match not found");

        }
        String playerWhitePath = document.get("playerWhite", String.class);
        String playerBlackPath = document.get("playerBlack", String.class);
        
        String playerWhiteId = playerWhitePath != null ? extractPlayerIdFromPath(playerWhitePath) : null;
        String playerBlackId = playerBlackPath != null ? extractPlayerIdFromPath(playerBlackPath) : null;

        if (playerWhiteId == null || playerBlackId == null || playerBlackId.equals("") || playerWhiteId.equals("")) {
            throw new Exception("Player not found");
        }
        Board board = new Board();
        GameState whiteGameState = new GameState(EPlayer.WHITE, board);

        simpMessagingTemplate.convertAndSend("/topic/match/" + currentMatchId, whiteGameState);
    }
}

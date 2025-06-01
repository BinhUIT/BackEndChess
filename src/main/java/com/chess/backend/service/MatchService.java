package com.chess.backend.service;

import java.security.SecureRandom;
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

import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.model.enums.EMatchState;
import com.chess.backend.model.enums.EMatchType;
import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.GameState;
import com.chess.backend.referencemodel.MatchReferenceModel;
import com.chess.backend.repository.FireBaseMatchRepository;
import com.chess.backend.repository.FireBasePlayerRepository;
import com.chess.backend.request.ChatRequest;
import com.chess.backend.request.CreateMatchRequest;
import com.chess.backend.request.MoveRequest;
import com.chess.backend.response.MatchResponse;
import com.google.api.client.util.Strings;
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
        String matchId = generateMatchId();
        String playerPath = "/User/" + request.getPlayerID();
        boolean playAsWhite = request.isPlayAsWhite();
        Integer playTime = request.getPlayTime();
        EMatchType matchType = request.getMatchType();

        Map<String, Object> data = new HashMap<>();
        data.put("matchId", matchId);
        data.put("matchState", EMatchState.WAITING_FOR_PLAYER);
        data.put("playerWhite", playAsWhite ? playerPath : null);
        data.put("playerBlack", playAsWhite ? null : playerPath);
        data.put("matchType", EMatchType.PRIVATE);
        data.put("playTime", playTime);
        data.put("numberOfTurns", 0);
        data.put("matchTime", new Date());

        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.WAITING_FOR_PLAYER);
        matchReferenceModel.setMatchType(matchType);
        matchReferenceModel.setPlayTime(playTime);
        matchReferenceModel.setMatchTime(new Date());
        matchReferenceModel.setNumberOfTurns(0);

        Match match = new Match(matchReferenceModel);
        Player player = playerRepository.findPlayerById(request.getPlayerID());
        synchronized (match) {
            if (playAsWhite) {
                match.setPlayerWhite(player);
            } else {
                match.setPlayerBlack(player);
            }
        }
        try {
            firestore.collection("Match").document(matchId).set(data);
        } catch (Exception e) {
            System.err.println("    ❌ Firestore save failed: " + e.getMessage());
        }

        return match;
    }
    // public Match createPrivateMatch(CreateMatchRequest request) {
    // MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
    // String matchId = UUID.randomUUID().toString();
    // String playerPath = "/User/" + request.getPlayerID();
    // boolean playAsWhite = request.isPlayAsWhite();
    // Integer playTime = request.getPlayTime();
    // EMatchType matchType = request.getMatchType();

    // // Tao HashMap de luu len FireBase
    // Map<String, Object> data = new HashMap<>();
    // data.put("matchId", matchId);
    // data.put("matchState", EMatchState.WAITING_FOR_PLAYER);
    // data.put("playerWhite", playAsWhite ? playerPath : null);
    // data.put("playerBlack", playAsWhite ? null : playerPath);
    // data.put("matchType", EMatchType.PRIVATE);
    // data.put("playTime", playTime);
    // data.put("numberOfTurns", 0);
    // data.put("matchTime", new Date());

    // matchReferenceModel.setMatchId(matchId);
    // matchReferenceModel.setMatchState(EMatchState.WAITING_FOR_PLAYER);
    // matchReferenceModel.setMatchType(matchType);
    // matchReferenceModel.setPlayTime(playTime);
    // matchReferenceModel.setMatchTime(new Date());
    // matchReferenceModel.setNumberOfTurns(0);

    // Match match = new Match(matchReferenceModel);

    // Player player = playerRepository.findPlayerById(request.getPlayerID());

    // // set lai playerWhite va playerBlack
    // if (request.isPlayAsWhite()) {
    // match.setPlayerWhite(player);
    // } else {
    // match.setPlayerBlack(player);
    // }

    // firestore.collection("Match").document(matchId).set(data);

    // return match;
    // }

    public Match createRankedMatch(CreateMatchRequest request, Player opponent) {
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        String matchId = generateMatchId();
        String playerPath = "/User/" + request.getPlayerID();
        String opponentPath = "/User/" + opponent.getPlayerId();
        // random 0 hoặc 1
        int randomInt = (int) (Math.random() * 2);
        // Tao HashMap de luu len FireBase
        Map<String, Object> data = new HashMap<>();
        data.put("matchId", matchId);
        data.put("matchState", EMatchState.IN_PROGRESS);
        data.put("playerWhite", randomInt == 0 ? playerPath : opponentPath);
        data.put("playerBlack", randomInt == 0 ? opponentPath : playerPath);
        data.put("matchType", EMatchType.RANKED);
        data.put("playTime", request.getPlayTime());
        data.put("numberOfTurns", 0);
        data.put("matchTime", new Date());

        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.IN_PROGRESS);
        matchReferenceModel.setMatchType(request.getMatchType());
        matchReferenceModel.setPlayTime(request.getPlayTime());
        matchReferenceModel.setMatchTime(new Date());
        matchReferenceModel.setNumberOfTurns(0);

        Match match = new Match(matchReferenceModel);

        Player player = playerRepository.findPlayerById(request.getPlayerID());

        // set lai playerWhite va playerBlack
        if (randomInt==0) {
            match.setPlayerWhite(player);
            match.setPlayerBlack(opponent);
        } else {
            match.setPlayerWhite(opponent);
            match.setPlayerBlack(player);
        }

        firestore.collection("Match").document(matchId).set(data);
        return match;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generateMatchId() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public Match joinMatch(String matchId, String playerId) throws InterruptedException, ExecutionException {
        return firestore.runTransaction(transaction -> {
            DocumentSnapshot document = getDataService.GetDataSnapShot("Match", matchId);
            if (!document.exists()) {
                throw new RuntimeException("Match not found");
            }

            EMatchState currentState = EMatchState.valueOf(document.getString("matchState"));
            if (currentState != EMatchState.WAITING_FOR_PLAYER)
                throw new RuntimeException("Match not joinable. State: " + currentState);

            // Xác định vị trí người chơi (trắng hoặc đen)
            String playerWhitePath = document.getString("playerWhite");
            String playerBlackPath = document.getString("playerBlack");
            Map<String, Object> updates = new HashMap<>();
            String playerPath = "/User/" + playerId;

            if (Strings.isNullOrEmpty(playerWhitePath)) {
                updates.put("playerWhite", playerPath);
            } else if (Strings.isNullOrEmpty(playerBlackPath)) {
                updates.put("playerBlack", playerPath);
            } else {
                throw new RuntimeException("Match is full");
            }

            // Cập nhật trạng thái match
            boolean isNowFull = !Strings.isNullOrEmpty(playerWhitePath) && !Strings.isNullOrEmpty(playerBlackPath);
            EMatchState newState = isNowFull
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
            matchReferenceModel.setPlayTime(document.getLong("playTime").intValue());

            Match match = new Match(matchReferenceModel);
            String playerWhiteId = (playerWhitePath==null)? playerId: extractPlayerIdFromPath(playerWhitePath);
            String playerBlackId = (playerBlackPath==null)? playerId: extractPlayerIdFromPath(playerBlackPath);

            // Thêm các người chơi vào match
            match.setPlayerWhite(fetchPlayer(playerWhiteId));
            match.setPlayerBlack(fetchPlayer(playerBlackId));

            // Kiểm tra xem có người chơi nào không
            if (match.getPlayerWhite() == null || match.getPlayerBlack() == null) {
                throw new RuntimeException("Player not found");
            }

            return match;
        }).get();
    }

    private Player fetchPlayer(String playerId) {
        Player player = playerRepository.findPlayerById(playerId);
        if (player == null)
            throw new RuntimeException("Player not found: " + playerId);
        return player;
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
        // Lay playerId tu path
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
        simpMessagingTemplate.convertAndSend("/chess/move/" + request.getCurrentMatchId(), request.getGameState());
    }

    public void StartGame(String currentMatchId) throws InterruptedException, ExecutionException, Exception {
        DocumentSnapshot document;
        try {
            document = getDataService.GetDataSnapShot("Matches", currentMatchId);
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

    public void PlayerChat(ChatRequest request) throws InterruptedException, ExecutionException, Exception {
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
        simpMessagingTemplate.convertAndSend("/topic/match/" + request.getCurrentMatchId(),
                request.getMessageContent());
    }

    public MatchResponse cancelMatch(String matchId, String playerId) throws InterruptedException, ExecutionException {
        DocumentSnapshot document = getDataService.GetDataSnapShot("Match", matchId);

        String playerWhitePath = document.getString("playerWhite");
        String playerBlackPath = document.getString("playerBlack");

        MatchResponse response = new MatchResponse();
        response.setMatchId(matchId);
        response.setMatchState(EMatchState.WAITING_FOR_PLAYER.toString());
        response.setNumberOfTurns(document.getLong("numberOfTurns").intValue());
        response.setPlayTime(document.getLong("playTime").intValue());
        response.setMatchTime(document.getDate("matchTime"));
        response.setPlayerBlackId(document.getString("playerBlack"));
        response.setPlayerWhiteId(document.getString("playerWhite"));
        Map<String, Object> updates = new HashMap<>();

        String playerPath = "/User/" + playerId;

        if (playerWhitePath != null && playerWhitePath.equals(playerPath)) {
            updates.put("playerWhite", null);
            response.setPlayerWhiteId(null);
        }
        if (playerBlackPath != null && playerBlackPath.equals(playerPath)) {
            updates.put("playerBlack", null);
            response.setPlayerBlackId(null);
        }
        updates.put("matchState", EMatchState.WAITING_FOR_PLAYER.toString());

        // update match len firebase
        firestore.collection("Match").document(matchId).update(updates);

        // tra match ve kenh
        response.setMatchState(EMatchState.WAITING_FOR_PLAYER.toString());
        return response;
    }
}
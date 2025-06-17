package com.chess.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
import com.chess.backend.request.EndMatchRequest;
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

    public Match createPrivateMatch(CreateMatchRequest request, boolean debug) {
        if (!debug)
            return createPrivateMatch(request);

        System.out.println("Starting createPrivateMatch...");

        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        System.out.println("Created new MatchReferenceModel");

        String matchId = generateMatchId();
        System.out.println("Generated match ID: " + matchId);

        String playerId = request.getPlayerID();
        System.out.println("Get player ID: " + playerId);

        String playerPath = "/User/" + request.getPlayerID();
        System.out.println("Created player path: " + playerPath);

        boolean playAsWhite = request.isPlayAsWhite();
        System.out.println("Player wants to play as white: " + playAsWhite);

        Integer playTime = request.getPlayTime() != null ? request.getPlayTime() : 0;
        System.out.println("Play time set to: " + playTime);

        EMatchType matchType = request.getMatchType();
        System.out.println("Match type: " + matchType);

        Map<String, Object> data = new HashMap<>();
        System.out.println("Creating data map for Firestore...");

        data.put("matchId", matchId);
        data.put("matchState", EMatchState.WAITING_FOR_PLAYER);
        data.put("playerWhite", playAsWhite ? playerPath : null);
        data.put("playerBlack", playAsWhite ? null : playerPath);
        data.put("matchType", EMatchType.PRIVATE);
        data.put("playTime", playTime);
        data.put("numberOfTurns", 0);
        data.put("matchTime", new Date());
        System.out.println("Data map populated: " + data);

        matchReferenceModel.setMatchId(matchId);
        matchReferenceModel.setMatchState(EMatchState.WAITING_FOR_PLAYER);
        matchReferenceModel.setMatchType(matchType);
        matchReferenceModel.setPlayTime(playTime);
        matchReferenceModel.setMatchTime(new Date());
        matchReferenceModel.setNumberOfTurns(0);
        System.out.println("MatchReferenceModel populated");

        Match match = new Match(matchReferenceModel);
        System.out.println("Created new Match object");

        try {
            Player player = fetchPlayer(playerId);
            if (playAsWhite) {
                match.setPlayerWhite(player);
                System.out.println("Assigned player as white");
            } else {
                match.setPlayerBlack(player);
                System.out.println("Assigned player as black");
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to fetch player: " + e.getMessage());
        }

        try {
            System.out.println("Attempting to save to Firestore...");
            firestore.collection("Match").document(matchId).set(data);
            System.out.println("✅ Successfully saved to Firestore");
        } catch (Exception e) {
            System.err.println("    ❌ Firestore save failed: " + e.getMessage());
        }

        System.out.println("Returning match object");
        return match;
    }

    public Match createPrivateMatch(CreateMatchRequest request) {
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        String matchId = generateMatchId();
        String playerId = request.getPlayerID();
        String playerPath = "/User/" + playerId;
        boolean playAsWhite = request.isPlayAsWhite();
        Integer playTime = request.getPlayTime() != null ? request.getPlayTime() : 0;
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
        try {
            Player player = fetchPlayer(playerId); // Waits until the Future completes
            if (playAsWhite) {
                match.setPlayerWhite(player);
                System.out.println("Assigned player as white");
            } else {
                match.setPlayerBlack(player);
                System.out.println("Assigned player as black");
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to fetch player: " + e.getMessage());
        }

        try {
            firestore.collection("Match").document(matchId).set(data);
        } catch (Exception e) {
            System.err.println("    ❌ Firestore save failed: " + e.getMessage());
        }

        return match;
    }

    public Match createRankedMatch(CreateMatchRequest request, Player opponent) {
        MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
        String matchId = generateMatchId();
        String playerId = request.getPlayerID();
        String opponentId = opponent.getPlayerId();
        String playerPath = "/User/" + playerId;
        String opponentPath = "/User/" + opponentId;
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

        try {
            Player player = fetchPlayer(playerId); // Waits until the Future completes
            // set lai playerWhite va playerBlack
            if (randomInt == 0) {
                match.setPlayerWhite(player);
                match.setPlayerBlack(opponent);
            } else {
                match.setPlayerWhite(opponent);
                match.setPlayerBlack(player);
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to fetch player: " + e.getMessage());
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
            updates.put("matchState", EMatchState.IN_PROGRESS);

            // Cập nhật dữ liệu trong Firebase
            firestore.collection("Match").document(matchId).update(updates);

            // Tạo và trả về đối tượng Match
            MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
            matchReferenceModel.setMatchId(matchId);
            matchReferenceModel.setMatchState(EMatchState.IN_PROGRESS);
            matchReferenceModel.setNumberOfTurns(document.getLong("numberOfTurns").intValue());
            matchReferenceModel.setMatchTime(document.getDate("matchTime"));
            matchReferenceModel.setPlayTime(document.getLong("playTime").intValue());

            Match match = new Match(matchReferenceModel);
            String playerWhiteId = (playerWhitePath == null) ? playerId : extractPlayerIdFromPath(playerWhitePath);
            String playerBlackId = (playerBlackPath == null) ? playerId : extractPlayerIdFromPath(playerBlackPath);

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
        try {
            CompletableFuture<Player> playerFuture = playerRepository.findPlayerByIdAsync(playerId);
            Player player = playerFuture.get(); // Waits until the Future completes
            if (player == null) {
                System.err.println("❌ Player not found with ID: " + playerId);
                throw new RuntimeException("Player not found");
            }
            System.out.println("Found player: " + player);
            return player;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to fetch player: " + e.getMessage());
        }
        return null; // Trả về null nếu không tìm thấy người chơi
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

        try {
            if (playerWhiteId != null) {
                Player whitePlayer = fetchPlayer(playerWhiteId);
                match.setPlayerWhite(whitePlayer);
            }

            if (playerBlackId != null) {
                Player blackPlayer = fetchPlayer(playerBlackId);
                match.setPlayerBlack(blackPlayer);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch players: " + e.getMessage());
            return null; // Trả về null nếu không tìm thấy người chơi
        }

        return match;
    }

    public CompletableFuture<Match> getMatchByIdAsync(String matchId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.fireBaseMatchRepository.getMatchById(matchId);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to fetch match: " + e.getMessage(), e);
            }
        });
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

        String playerWhitePath = document.getString("playerWhite");
        String playerBlackPath = document.getString("playerBlack");

        // Tạo MatchReferenceModel
        Map<String, Object> updates = new HashMap<>();

        updates.put("matchId", document.getString("matchId"));
        updates.put("matchState", EMatchState.valueOf(document.getString("matchState")).name());
        Date utilDate = document.getDate("matchTime");
        updates.put("matchTime", (utilDate != null) ? new Date(utilDate.getTime()) : null);
        updates.put("matchType", EMatchType.valueOf(document.getString("matchType")).name());
        updates.put("numberOfTurns", document.getLong("numberOfTurns").intValue() + 1); // tăng lượt đi
        updates.put("playTime", document.getLong("playTime").intValue());
        updates.put("playerWhite", playerWhitePath); // DocumentReference đúng kiểu
        updates.put("playerBlack", playerBlackPath); // DocumentReference đúng kiểu

        firestore.collection("Match").document(document.getString("matchId")).update(updates);
        String chessMoveTopic = "/topic/chess/move/" + request.getCurrentMatchId();
        System.out.println("Sending move message to topic: " + chessMoveTopic);
        System.out.println("Move request: " + request);
        simpMessagingTemplate.convertAndSend(chessMoveTopic, request);
    }

    public void StartGame(MatchResponse matchResponse) throws InterruptedException, ExecutionException, Exception {
        String chessStartTopic = "/topic/chess/start/" + matchResponse.getMatchId();
        System.out.println("Sending start game message to topic: " + chessStartTopic);
        simpMessagingTemplate.convertAndSend(chessStartTopic, matchResponse);
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

    public void deleteMatch(String matchId) throws ExecutionException, InterruptedException {
        firestore.collection("Match").document(matchId).delete();
    }

    public void endMatch(EndMatchRequest request) throws InterruptedException, ExecutionException {
        DocumentSnapshot match = getDataService.GetDataSnapShot("Match", request.getMatchId());

        if (match == null || !match.exists()) {
            throw new IllegalArgumentException("Match not found.");
        }

        Map<String, Object> update = new HashMap<>();
        update.put("matchState", request.getResult());
        String playerWhiteId = extractPlayerIdFromPath(match.getString("playerWhite"));
        String playerBlackId = extractPlayerIdFromPath(match.getString("playerBlack"));

        if (request.getType().equals("RANKED")) {
            DocumentSnapshot playerWhite = getDataService.GetDataSnapShot("User", playerWhiteId);
            DocumentSnapshot playerBlack = getDataService.GetDataSnapShot("User", playerBlackId);

            Long BlackElo = playerBlack.getLong("score");
            Long WhiteElo = playerWhite.getLong("score");

            if (BlackElo == null)
                BlackElo = (long) 0;
            if (WhiteElo == null)
                WhiteElo = (long) 0;

            int BlackK = getCoefficientK(BlackElo);
            int WhiteK = getCoefficientK(WhiteElo);

            BigDecimal EBlack = new BigDecimal(Math.pow(10, BlackElo / 400)).setScale(3, RoundingMode.HALF_UP);
            BigDecimal EWhite = new BigDecimal(Math.pow(10, WhiteElo / 400)).setScale(3, RoundingMode.HALF_UP);

            // Gắn mặc định là điểm hiện tại
            Long newBlackScore = BlackElo;
            Long newWhiteScore = WhiteElo;

            if (request.getResult().equals(EMatchState.BLACK_WIN)) {
                double blackChange = calculatePoint(EBlack.doubleValue(), BlackK, 1);
                double whiteChange = calculatePoint(EWhite.doubleValue(), WhiteK, 0);
                newBlackScore = Math.round(BlackElo + blackChange);
                newWhiteScore = Math.round(WhiteElo + whiteChange);
            } else if (request.getResult().equals(EMatchState.WHITE_WIN)) {
                double blackChange = calculatePoint(EBlack.doubleValue(), BlackK, 0);
                double whiteChange = calculatePoint(EWhite.doubleValue(), WhiteK, 1);
                newBlackScore = Math.round(BlackElo + blackChange);
                newWhiteScore = Math.round(WhiteElo + whiteChange);
            } else if (request.getResult().equals(EMatchState.DRAW)) {
                double blackChange = calculatePoint(EBlack.doubleValue(), BlackK, 0.5);
                double whiteChange = calculatePoint(EWhite.doubleValue(), WhiteK, 0.5);
                newBlackScore = Math.round(BlackElo + blackChange);
                newWhiteScore = Math.round(WhiteElo + whiteChange);
            }
            updateScore(playerBlack, newBlackScore, request.getMatchId());
            updateScore(playerWhite, newWhiteScore, request.getMatchId());

            getDataService.updateAllUserRanks();
        }
    }

    // Lấy hệ số theo số điểm
    public int getCoefficientK(Long score) {
        if (score <= 1600)
            return 25;
        if (score <= 2000)
            return 20;
        if (score < 2400)
            return 15;
        return 10;
    }

    // Tính điểm thay đổi
    public double calculatePoint(double E, int K, double A) {
        return K * (A - E);
    }

    public void updateScore(DocumentSnapshot player, Long point, String matchId) {
        Map<String, Object> updatePlayer = new HashMap<>();
        updatePlayer.put("score", point);
        firestore.collection("Match").document(matchId).update(updatePlayer);
    }
}
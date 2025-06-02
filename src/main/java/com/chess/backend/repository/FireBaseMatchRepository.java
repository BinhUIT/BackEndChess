package com.chess.backend.repository;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Repository;

import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.model.enums.EMatchState;
import com.chess.backend.model.enums.EMatchType;
import com.chess.backend.referencemodel.MatchReferenceModel;
import com.chess.backend.service.GetDataService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

@Repository
public class FireBaseMatchRepository implements MatchRepository {
    @Autowired
    private final FireBasePlayerRepository playerRepository;

    private Firestore dbFirestore;
    private final String COLLECTION_NAME = "Match";

    public FireBaseMatchRepository(FireBasePlayerRepository playerRepository) {
        this.dbFirestore = FirestoreClient.getFirestore();
        this.playerRepository = playerRepository;
    }

    @Override
    public void saveMatch(MatchReferenceModel match) {
        DocumentReference addedDocRef = dbFirestore.collection(COLLECTION_NAME).document(match.getMatchId());
        ApiFuture<WriteResult> writeResult = addedDocRef.set(match);
        try {
            writeResult.get(); // Wait for the operation to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save match", e);
        }
    }

    @Override
    public void updateMatch(MatchReferenceModel match) {

    }

    @Override
    public Match getMatchById(String matchId) {
        try {
            DocumentSnapshot document = dbFirestore.collection(COLLECTION_NAME).document(matchId).get().get();

            System.out.println("DocumentSnapshot data: " + document.getData() +
                    " with docs ID: " + document.getId() +
                    " and original ID: " + matchId +
                    " exists: " + document.exists());

            System.out.println("DocumentSnapshot data: " + document.getData() + " with docs ID: " + document.getId()
                    + " and original ID: " + matchId + " and document exists: " + document.exists());
            // Kiểm tra xem tài liệu có tồn tại không
            if (document.exists()) {
                String mId = document.getString("matchId");
                EMatchState matchState = EMatchState.valueOf(document.getString("matchState"));

                Date utilDate = document.getDate("matchTime");
                Date matchTime = (utilDate != null) ? new Date(utilDate.getTime()) : null;

                EMatchType matchType = EMatchType.valueOf(document.getString("matchType"));
                int numberOfTurns = document.getLong("numberOfTurns").intValue();
                int playTime = document.getLong("playTime").intValue();

                String playerWhitePath = document.getString("playerWhite");
                String playerBlackPath = document.getString("playerBlack");
                String playerWhiteId = playerWhitePath != null ? extractPlayerIdFromPath(playerWhitePath) : null;
                String playerBlackId = playerBlackPath != null ? extractPlayerIdFromPath(playerBlackPath) : null;

                MatchReferenceModel matchReferenceModel = new MatchReferenceModel();
                matchReferenceModel.setMatchId(mId);
                matchReferenceModel.setMatchState(matchState);
                matchReferenceModel.setMatchTime(matchTime);
                matchReferenceModel.setMatchType(matchType);
                matchReferenceModel.setNumberOfTurns(numberOfTurns);
                matchReferenceModel.setPlayTime(playTime);
                Match match = new Match(matchReferenceModel);
                try {
                    if (playerWhiteId != null) {
                        match.setPlayerWhite(fetchPlayer(playerWhiteId));
                    }
                    if (playerBlackId != null) {
                        match.setPlayerBlack(fetchPlayer(playerBlackId));
                    }
                } catch (RuntimeException e) {
                    System.err.println("Failed to fetch player: " + e.getMessage());
                    return null; // Trả về null nếu không tìm thấy người chơi
                }

                return match;
                // Process the match object as needed
            } else {
                // Handle the case where the match does not exist
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to retrieve match", e);
        }
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
}
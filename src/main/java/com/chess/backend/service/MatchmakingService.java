package com.chess.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.chess.backend.config.FirebaseAuthInterceptor;
import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.model.enums.EMatchType;
import com.chess.backend.referencemodel.QueuedRankPlayer;
import com.chess.backend.request.CreateMatchRequest;
import com.chess.backend.response.MatchResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    @Autowired
    private final FirebaseAuthInterceptor firebaseAuthInterceptor;

    private final List<QueuedRankPlayer> playerQueue = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private final MatchService matchService;

    private final SimpMessagingTemplate messagingTemplate;

    public boolean addPlayerToQueue(Player player, CreateMatchRequest request) {
        if (player == null || player.getPlayerId() == null || request == null) {
            return false;
        }

        // Kiểm tra xem người chơi đã có trong hàng đợi chưa
        synchronized (playerQueue) {
            boolean playerExists = playerQueue.stream()
                    .anyMatch(qp -> qp.getPlayer().getPlayerId().equals(player.getPlayerId()));

            if (playerExists) {
                // Người chơi đã trong hàng đợi, có thể cập nhật thông tin nếu cần
                playerQueue.removeIf(qp -> qp.getPlayer().getPlayerId().equals(player.getPlayerId()));
            }

            // Thêm người chơi với thông tin mới vào hàng đợi
            playerQueue.add(new QueuedRankPlayer(
                    player,
                    request.getPlayTime(),
                    request.getMatchType(),
                    System.currentTimeMillis()));
        }

        return true;
    }

    public void removePlayersFromQueue(Player... players) {
        if (players == null)
            return;

        synchronized (playerQueue) {
            for (Player player : players) {
                if (player != null && player.getPlayerId() != null) {
                    playerQueue.removeIf(qp -> qp.getPlayer().getPlayerId().equals(player.getPlayerId()));
                }
            }
        }
    }

    public Match findMatch(Player player) {
        System.out.println("------------------goi ham tim tran rank 1-----------------");
        if (player == null) {
            return null;
        }

        // tim player trong hang doi
        QueuedRankPlayer currentQueuedPlayer = null;
        synchronized (playerQueue) {
            for (QueuedRankPlayer qp : playerQueue) {
                if (qp.getPlayer().getPlayerId().equals(player.getPlayerId())) {
                    currentQueuedPlayer = qp;
                    break;
                }
            }
        }
        System.out.println("------------------goi ham tim tran rank 2-----------------");

        // Nếu không tìm thấy người chơi trong hàng đợi
        if (currentQueuedPlayer == null) {
            return null;
        }

        int playerRank = player.getRank();
        Integer requestedPlayTime = currentQueuedPlayer.getPlayTime(); // Thời gian chơi mong muốn
        EMatchType matchType = currentQueuedPlayer.getMatchType(); // Loại trận đấu

        int rankThreshold = 100; // Ngưỡng chênh lệch rank

        // Tìm đối thủ phù hợp trong hàng đợi
        synchronized (playerQueue) {
            System.out.println("------------------goi ham tim tran rank 3-----------------");
            for (QueuedRankPlayer queuedOpponent : playerQueue) {
                Player opponent = queuedOpponent.getPlayer();

                // Không ghép với chính mình
                if (opponent.getPlayerId().equals(player.getPlayerId())) {
                    continue;
                }

                // Kiểm tra timePlay có khớp chính xác không
                boolean timePlayMatches = Objects.equals(requestedPlayTime, queuedOpponent.getPlayTime());

                // Kiểm tra rank có phù hợp không
                boolean rankMatches = Math.abs(opponent.getRank() - playerRank) <= rankThreshold;

                // Chỉ ghép cặp khi tất cả các điều kiện đều thỏa mãn
                if (timePlayMatches && rankMatches) {
                    try {
                        System.out.println("-----------Thoi gian cua tran dau: " + timePlayMatches);
                        // Tạo trận đấu xếp hạng
                        CreateMatchRequest request = new CreateMatchRequest();
                        request.setMatchType(EMatchType.RANKED);
                        request.setPlayTime(requestedPlayTime);
                        request.setPlayerID(player.getPlayerId());
                        Match match = matchService.createRankedMatch(request, opponent);

                        if (match != null) {
                            // Xóa cả hai người chơi khỏi hàng đợi
                            removePlayersFromQueue(player, opponent);
                            return match;
                        }
                    } catch (Exception e) {
                        System.err.println("Error creating match: " + e.getMessage());
                    }
                }
            }
        }

        // Không tìm thấy đối thủ phù hợp
        // Người chơi đã được thêm vào hàng đợi ở đầu phương thức
        return null;
    }

    // Phương thức định kỳ kiểm tra hàng đợi và ghép cặp người chơi
    @Scheduled(fixedRate = 5000) // Chạy mỗi 5 giây
    public void processMatchmakingQueue() {
        List<String> processedPlayers = new ArrayList<>();
        synchronized (playerQueue) {
            for (QueuedRankPlayer pq : playerQueue) {
                // Bỏ qua người chơi đã được xử lý
                if (processedPlayers.contains(pq.getPlayer().getPlayerId())) {
                    continue;
                }

                Match match = findMatch(pq.getPlayer());
                if (match != null) {
                    // Gửi thông tin match cho cả hai người chơi
                    messagingTemplate.convertAndSendToUser(match.getPlayerWhite().getPlayerId(),
                            "/queue/match", new MatchResponse(match));
                    messagingTemplate.convertAndSendToUser(match.getPlayerBlack().getPlayerId(),
                            "/queue/match", new MatchResponse(match));

                    // Đánh dấu cả hai người chơi đã được xử lý
                    processedPlayers.add(match.getPlayerWhite().getPlayerId());
                    processedPlayers.add(match.getPlayerBlack().getPlayerId());

                    // Xóa cả hai người chơi khỏi hàng đợi
                    removePlayersFromQueue(match.getPlayerWhite(), match.getPlayerBlack());
                }
            }
        }
    }
}
package com.chess.backend.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chess.backend.ServerApplication;
import com.chess.backend.model.EMatchType;
import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.request.ChatRequest;
import com.chess.backend.request.CreateMatchRequest;
import com.chess.backend.request.JoinMatchRequest;
import com.chess.backend.request.MoveRequest;
import com.chess.backend.response.MatchResponse;
import com.chess.backend.service.MatchService;
import com.chess.backend.service.MatchmakingService;
import com.chess.backend.service.PlayerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final ServerApplication serverApplication;
    @Autowired
    private MatchService matchService;

    @Autowired
    private final MatchmakingService matchmakingService;

    @Autowired
    private final PlayerService playerService;
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> GetAllMatch() {
        List<Match> listMatch = new ArrayList<>();
        try {
            listMatch = matchService.listMatch();
            return new ResponseEntity<>(listMatch, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @MessageMapping("/chess/create")
    public void createMatch(@RequestBody CreateMatchRequest request, Principal principal) {
        try {
            if (request.getMatchType().equals(EMatchType.PRIVATE)) {
                // Xử lý tạo match private
                Match match = matchService.createPrivateMatch(request);
                if (match != null) {
                    // Gửi thông tin match trực tiếp cho người tạo
                    messagingTemplate.convertAndSendToUser(request.getPlayerID(), "/queue/match", new MatchResponse(match));
                } else {
                    messagingTemplate.convertAndSendToUser(request.getPlayerID(), "/queue/match/error",
                            new MatchResponse("ERROR: Private match cannot be created"));
                }
            } else if (request.getMatchType().equals(EMatchType.RANKED)) {
                // Xử lý tạo match ranked - thêm người chơi vào hàng đợi
                Player player = playerService.GetPlayerById(request.getPlayerID());

                // Thêm người chơi vào hàng đợi tìm trận
                boolean addedToQueue = matchmakingService.addPlayerToQueue(player, request);

                if (addedToQueue) {
                    // Thông báo cho người chơi rằng họ đã được thêm vào hàng đợi
                    messagingTemplate.convertAndSendToUser(request.getPlayerID(), "/queue/match",
                            new MatchResponse("Added to matchmaking queue"));

                    // Tìm người chơi phù hợp và tạo match nếu có
                    Match match = matchmakingService.findMatch(player);

                    if (match != null) {
                        // Gửi thông tin match cho cả hai người chơi
                        messagingTemplate.convertAndSendToUser(match.getPlayerBlack().getPlayerId(),
                                "/queue/match", new MatchResponse(match));
                        messagingTemplate.convertAndSendToUser(match.getPlayerWhite().getPlayerId(),
                                "/queue/match", new MatchResponse(match));

                        // Xóa cả hai người chơi khỏi hàng đợi
                        matchmakingService.removePlayersFromQueue(match.getPlayerBlack(), match.getPlayerWhite());
                        // Gửi GameState tới topic/match
                        matchService.StartGame(match.getMatchId());
                    }
                    // Nếu không tìm thấy match, người chơi vẫn ở trong hàng đợi
                } else {
                    messagingTemplate.convertAndSendToUser(request.getPlayerID(), "/queue/match/error",
                            new MatchResponse("ERROR: Cannot join matchmaking queue"));
                }
            }
        } catch (Exception ex) {
            messagingTemplate.convertAndSendToUser(request.getPlayerID(), "/queue/match/error",
                    new MatchResponse("INTERNAL_SERVER_ERROR: " + ex.getMessage()));
        }
    }

    @MessageMapping("/chess/join/{matchId}")
    public void joinMatch(@DestinationVariable String matchId, JoinMatchRequest request) {
        try {
            Match updateMatch = matchService.joinMatch(matchId, request.getPlayerId());

            messagingTemplate.convertAndSend("/topic/match/" + matchId, new MatchResponse(updateMatch));
        } catch (Exception ex) {
            messagingTemplate.convertAndSend("/topic/match/" + matchId + "/error", ex.getMessage());
        }
    }

    @MessageMapping("/chess/get/{matchId}")
    @SendToUser("/queue/match/info")
    public MatchResponse getMatchInfo(@DestinationVariable String matchId, Principal principal) {
        try {
            Match match = matchService.getMatchById(matchId);
            if (match != null) {
                return new MatchResponse(match);
            } else {
                return new MatchResponse("Match not found");
            }
        } catch (Exception e) {
            return new MatchResponse("INTERNAL SERVER ERROR: " + e.getMessage());
        }
    }

    @MessageMapping("/chess/start")
    public void StartMatch(@Payload String currentMatchId) {
        try {
            matchService.StartGame(currentMatchId);
            return ;
        } catch (Exception e) {
           e.printStackTrace();
           return;
        }
    }

    @MessageMapping("/chess/move")
    public void Move(@Payload MoveRequest request) {
        try {
            matchService.PlayerMove(request);
            return ;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ;
        } catch (Exception e) {
            if (e.getMessage().equals("Match not found")) {
                return ;
            }
            e.printStackTrace();
            return ;
        }
    }
    @MessageMapping("/chess/chat") 
    public void Chat(@Payload ChatRequest request) {
        try {
            matchService.PlayerChat(request);
            return ;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ;
        } catch (Exception e) {
            if (e.getMessage().equals("Match not found")) {
                return ;
            }
            e.printStackTrace();
            return ;
        }
    }

}

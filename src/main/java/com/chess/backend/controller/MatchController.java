package com.chess.backend.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.model.enums.EMatchType;
import com.chess.backend.request.CancelMatchRequest;
import com.chess.backend.request.ChatRequest;
import com.chess.backend.request.CreateMatchRequest;
import com.chess.backend.request.EndMatchRequest;
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
    @Autowired
    private MatchService matchService;

    @Autowired
    private final MatchmakingService matchmakingService;

    @Autowired
    private final PlayerService playerService;
    @Autowired
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
    public void createMatch(CreateMatchRequest request, Principal principal, Message<?> message) {
        System.out.println("🔥 Principal: " + (principal != null ? principal.getName() : "null"));
        System.out.println("📦 Headers: " + message.getHeaders());
        try {
            EMatchType matchType = request.getMatchType();
            if (matchType == null) {
                messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/match/error",
                        new MatchResponse("ERROR: Match type is required"));
                return;
            }

            if (matchType.equals(EMatchType.PRIVATE)) {
                // Xử lý tạo match private
                System.out.print("Creating match: ");
                Match match = matchService.createPrivateMatch(request);
                System.out.print(match + "\n");
                System.out.println();
                if (match != null) {
                    // Gửi thông tin match trực tiếp cho người tạo
                    System.out.println("request playerID = " + request.getPlayerID());
                    System.out.println("Match Response: ");
                    System.out.println(match.getMatchId());
                    messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/match",
                            new MatchResponse(match));
                } else {
                    messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/match/error",
                            new MatchResponse("ERROR: Private match cannot be created"));
                }
            } else if (matchType.equals(EMatchType.RANKED)) {
                // Xử lý tạo match ranked - thêm người chơi vào hàng đợi
                System.out.println("----------------SessionID : " + principal.getName());
                System.out.println("----------------PlayerID : " + principal.getName());
                Player player = playerService.GetPlayerById(request.getPlayerID());

                // Thêm người chơi vào hàng đợi tìm trận
                boolean addedToQueue = matchmakingService.addPlayerToQueue(player, request);

                if (addedToQueue) {
                    // Thông báo cho người chơi rằng họ đã được thêm vào hàng đợi
                    messagingTemplate.convertAndSend("/topic/rank-match/" + player.getPlayerId(),
                            "Added to matchmaking queue");

                    // Tìm người chơi phù hợp và tạo match nếu có
                    Match match = matchmakingService.findMatch(player);

                    if (match != null) {
                        System.out.println("👉 Creating match between:");
                        System.out.println("👉 Creating match between:");
                        System.out.println("White: "
                                + (match.getPlayerWhite() != null ? match.getPlayerWhite().getPlayerId() : "null"));
                        System.out.println("Black: "
                                + (match.getPlayerBlack() != null ? match.getPlayerBlack().getPlayerId() : "null"));
                        System.out.println("Principal: " + principal.getName());

                        // Gửi thông tin match cho cả hai người chơi
                        messagingTemplate.convertAndSend("/topic/rank-match/" + match.getPlayerBlack().getPlayerId(),
                                new MatchResponse(match));
                        messagingTemplate.convertAndSend("/topic/rank-match/" + match.getPlayerWhite().getPlayerId(),
                                new MatchResponse(match));

                        // Xóa cả hai người chơi khỏi hàng đợi
                        matchmakingService.removePlayersFromQueue(match.getPlayerBlack(), match.getPlayerWhite());
                        // Gửi GameState tới topic/match
                        matchService.StartGame(match.getMatchId());
                    }
                    // Nếu không tìm thấy match, người chơi vẫn ở trong hàng đợi
                } else {
                    messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/match/error",
                            new MatchResponse("ERROR: Cannot join matchmaking queue"));
                }
            }
        } catch (Exception ex) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/match/error",
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
            Match match = matchService.getMatchByIdAsync(currentMatchId).get();
            System.out.println("Starting match with ID: " + currentMatchId);
            if (match != null) {
                matchService.StartGame(new MatchResponse(match));
            } else {
                matchService.StartGame(new MatchResponse("Match not found"));
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @MessageMapping("/chess/move/{matchId}")
    public void Move(@Payload MoveRequest request) {
        try {
            matchService.PlayerMove(request);
            return;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            if (e.getMessage().equals("Match not found")) {
                return;
            }
            e.printStackTrace();
            return;
        }
    }

    @MessageMapping("/chess/chat")
    public void Chat(@Payload ChatRequest request) {
        try {
            matchService.PlayerChat(request);
            return;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            if (e.getMessage().equals("Match not found")) {
                return;
            }
            e.printStackTrace();
            return;
        }
    }

    @MessageMapping("/chess/cancelMatch")
    public void cancelMatch(@Payload CancelMatchRequest request) {
        try {
            MatchResponse response = matchService.cancelMatch(request.getMatchId(), request.getPlayerId());
            messagingTemplate.convertAndSend("/topic/match/" + request.getMatchId(), response);
        } catch (Exception exception) {
            exception.printStackTrace(); // 👈 log server
            messagingTemplate.convertAndSend("/topic/match/" + request.getMatchId() + "/error",
                    exception.toString() + "\n" + Arrays.toString(exception.getStackTrace()));
        }
    }

    @MessageMapping("/chess/destroyMatch/{matchId}")
    public void destroyMatch(@DestinationVariable String matchId) throws InterruptedException, ExecutionException {
        Match match = matchService.getMatchByIdAsync(matchId).get();
        if (match == null)
            messagingTemplate.convertAndSend("/topic/match/" + matchId + "/error", "Phòng hiện tại không còn");
        matchService.deleteMatch(matchId);
        messagingTemplate.convertAndSend("/topic/match/" + matchId, "destroyed");
    }

    @MessageMapping("/chess/endMatch")
    public void endMatch(@Payload EndMatchRequest request) throws InterruptedException, ExecutionException {
        System.out.println("Request ket thuc tran dau -------------------");
        System.out.println(request);

        try{
        String matchId = request.getMatchId();
        matchService.endMatch(request);
        if (request.getType().equals("PRIVATE"))
            messagingTemplate.convertAndSend("/topic/match/" + matchId, "Match is completed");
        else
            messagingTemplate.convertAndSend("/topic/rank-match/" + matchId, "Match is completed");
        }catch(Exception exception){
            String matchId = request.getMatchId();
            if (request.getType().equals("PRIVATE"))
            messagingTemplate.convertAndSend("/topic/match/" + matchId+ "/error", exception.getMessage()!=null ? exception.getMessage() : "Không xác định");
        else
            messagingTemplate.convertAndSend("/topic/rank-match/" + matchId+ "/error",  exception.getMessage()!=null ? exception.getMessage() : "Không xác định");
        }
    }
}

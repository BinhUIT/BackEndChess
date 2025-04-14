package com.chess.backend.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chess.backend.ServerApplication;
import com.chess.backend.model.Match;
import com.chess.backend.request.CreateMatchRequest;
import com.chess.backend.request.JoinMatchRequest;
import com.chess.backend.response.MatchResponse;
import com.chess.backend.service.MatchService;

@RestController
public class MatchController {

    private final ServerApplication serverApplication;
    @Autowired
    private MatchService matchService;
    private SimpMessagingTemplate messagingTemplate;

    MatchController(ServerApplication serverApplication) {
        this.serverApplication = serverApplication;
    }

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

    @MessageMapping("chess/create")
    @SendTo("topic/match/created")
    public ResponseEntity<?> createMatch(CreateMatchRequest request) {
        try {
            Match match = matchService.createMatch(request);
            if (match != null)
                return ResponseEntity.ok().body(new MatchResponse(match));
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MatchResponse("Match Not Found"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MatchResponse(ex.getMessage()));
        }
    }

    @MessageMapping("/chess/join/{matchId}")
    public void joinMatch(@DestinationVariable String matchId, JoinMatchRequest request){
        try{
            Match updateMatch=matchService.joinMatch(matchId, request.getPlayerId());

            messagingTemplate.convertAndSend("/topic/match/" + matchId, new MatchResponse(updateMatch));
        }catch(Exception ex){
            messagingTemplate.convertAndSend("/topic/match/" + matchId + "/error",ex.getMessage());
        }
    }
    @MessageMapping("/chess/get/{matchId}")
    @SendToUser("/queue/match/info")
    public ResponseEntity<MatchResponse> getMatchInfo(@DestinationVariable String matchId) {
        try {
            Match match = matchService.getMatchById(matchId);
            if (match != null) {
                return ResponseEntity.ok(new MatchResponse(match));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MatchResponse("Match not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MatchResponse(e.getMessage()));
        }
    }

    @SubscribeMapping("/topic/match/{matchId}")
    public void subscribeToMatch(@DestinationVariable String matchId){

    }

}

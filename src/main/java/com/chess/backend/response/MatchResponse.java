package com.chess.backend.response;

import java.util.Date;

import com.chess.backend.model.Match;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchResponse {
    private String matchId;
    private String playerWhiteId;
    private String playerBlackId;
    private String matchState;
    private int numberOfTurns;
    private int playTime;
    private Date matchTime;
    private String ErrorMessage;
    
    public MatchResponse(Match match) {
        this.matchId = match.getMatchId();
        this.playerWhiteId = match.getPlayerWhite() != null ? match.getPlayerWhite().getId() : null;
        this.playerBlackId = match.getPlayerBlack() != null ? match.getPlayerBlack().getId() : null;
        this.matchState = match.getMatchState().toString();
        this.numberOfTurns = match.getNumberOfTurns();
        this.playTime=match.getPlayTime();
        this.matchTime = match.getMatchTime();
    }
    public MatchResponse(String error){
        this.ErrorMessage=error;
    }
}

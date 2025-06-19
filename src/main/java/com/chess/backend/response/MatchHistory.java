package com.chess.backend.response;

import java.util.Date;

import com.chess.backend.model.Player;

import lombok.Data;

@Data
public class MatchHistory {
    private String matchId;
    private Player playerWhite;
    private Player playerBlack;
    private String matchState;
    private String matchType;
    private Long playTime;
    private Long numberOfTurns;
    private Date matchTime;
}
package com.chess.backend.referencemodel;

import java.util.Date;

import lombok.Data;

@Data
public class MatchSnapshot {
    private String matchId;
    private String playerWhite;
    private String playerBlack;
    private String matchState;
    private String matchType;
    private Long playTime;
    private Long numberOfTurns;
    private Date matchTime;
}

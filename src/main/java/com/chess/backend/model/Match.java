package com.chess.backend.model;

import java.util.Date;

import com.chess.backend.referencemodel.MatchReferenceModel;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Match { 
    private String matchId;
    private Player playerWhite;
    private Player playerBlack;
    private EMatchState matchState;
    private int numberOfTurns;
    private Date matchTime;
    public Match(MatchReferenceModel matchReferenceModel) {
        this.matchId=matchReferenceModel.getMatchId();
        this.matchState=matchReferenceModel.getMatchState();
        this.playerWhite=null;
        this.playerBlack=null;
        this.numberOfTurns=matchReferenceModel.getNumberOfTurns();
        this.matchTime=matchReferenceModel.getMatchTime();
    }
}

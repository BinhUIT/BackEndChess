package com.chess.backend.model;

import com.chess.backend.referencemodel.MatchReferenceModel;
import com.google.cloud.firestore.DocumentReference;

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
    private MatchState matchState;
    private int numberOfTurns;
    public Match(MatchReferenceModel matchReferenceModel) {
        this.matchId=matchReferenceModel.getMatchId();
        this.matchState=matchReferenceModel.getMatchState();
        this.playerWhite=null;
        this.playerBlack=null;
        this.numberOfTurns=matchReferenceModel.getNumberOfTurns();
    }
}

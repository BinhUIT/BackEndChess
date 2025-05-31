package com.chess.backend.model;

import java.util.Date;

import com.chess.backend.model.enums.EMatchState;
import com.chess.backend.model.enums.EMatchType;
import com.chess.backend.referencemodel.MatchReferenceModel;
import com.google.firebase.database.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Match {
    @NotNull
    private String matchId;
    @NotNull
    private Player playerWhite;
    @NotNull
    private Player playerBlack;
    @NotNull
    private EMatchState matchState;
    @NotNull
    private EMatchType matchType;
    @NotNull
    private Integer playTime;
    @NotNull
    private int numberOfTurns;
    @NotNull
    private Date matchTime;

    public Match(MatchReferenceModel matchReferenceModel) {
        this.matchId = matchReferenceModel.getMatchId();
        this.matchState = matchReferenceModel.getMatchState();
        this.playerWhite = null;
        this.playerBlack = null;
        this.matchType = matchReferenceModel.getMatchType();
        this.playTime = matchReferenceModel.getPlayTime();
        this.numberOfTurns = matchReferenceModel.getNumberOfTurns();
        this.matchTime = matchReferenceModel.getMatchTime();
    }
}

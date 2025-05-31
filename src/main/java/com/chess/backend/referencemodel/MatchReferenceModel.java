package com.chess.backend.referencemodel;

import java.util.Date;

import com.chess.backend.model.enums.EMatchState;
import com.chess.backend.model.enums.EMatchType;
import com.google.cloud.firestore.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MatchReferenceModel {
    private String matchId;
    private DocumentReference playerWhite;
    private DocumentReference playerBlack;
    private EMatchState matchState;
    private EMatchType matchType;
    private int playTime;
    private int numberOfTurns;
    private Date matchTime;
}

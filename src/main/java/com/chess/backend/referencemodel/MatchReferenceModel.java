package com.chess.backend.referencemodel;

import com.chess.backend.model.MatchState;
import com.google.cloud.firestore.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MatchReferenceModel {
    private String matchId;
    private DocumentReference playerWhite;
    private DocumentReference playerBlack;
    private MatchState matchState;
    private int numberOfTurns;
}

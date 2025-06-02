package com.chess.backend.repository;

import com.chess.backend.model.Match;
import com.chess.backend.referencemodel.MatchReferenceModel;

public interface MatchRepository {
    void saveMatch(MatchReferenceModel match);

    void updateMatch(MatchReferenceModel match);

    Match getMatchById(String matchId);
}
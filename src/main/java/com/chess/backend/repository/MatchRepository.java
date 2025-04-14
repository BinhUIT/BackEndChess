package com.chess.backend.repository;

import com.chess.backend.model.Match;

public interface MatchRepository  {
    void saveMatch(Match match);
    
}
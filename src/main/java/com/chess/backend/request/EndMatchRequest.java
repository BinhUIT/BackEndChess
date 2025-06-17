package com.chess.backend.request;

import com.chess.backend.model.enums.EMatchState;

import lombok.Data;

@Data
public class EndMatchRequest {
    private String matchId;
    private EMatchState result;
    private String type;
}

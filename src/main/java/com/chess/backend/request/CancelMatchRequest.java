package com.chess.backend.request;

import lombok.Data;

@Data
public class CancelMatchRequest {
    private String matchId;
    private String playerId;
}

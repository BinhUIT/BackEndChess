package com.chess.backend.request;

import lombok.Data;

@Data
public class EndMatchRequest {
    private String matchId;
    private String result;
    private String type;
}

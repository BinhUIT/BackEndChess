package com.chess.backend.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String playerId;
    private String playerName;
    private String token;
}

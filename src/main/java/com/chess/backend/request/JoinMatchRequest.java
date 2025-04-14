package com.chess.backend.request;

import com.google.firebase.database.annotations.NotNull;

import lombok.Data;

@Data
public class JoinMatchRequest {
    @NotNull
    private String playerId;
}

package com.chess.backend.request;

import com.google.firebase.database.annotations.NotNull;

import lombok.Data;

@Data
public class CreateMatchRequest {
    @NotNull
    private String playerID;
    
    private boolean playAsWhite; // nguoi tao muon chon quan trang hay khong
}

package com.chess.backend.model.game_state;

public enum EPlayer {
    NONE,
    WHITE,
    BLACK;
    
    public EPlayer getOpponent() {
        return switch (this) {
            case WHITE -> BLACK;
            case BLACK -> WHITE;
            default -> NONE;
        };
    }
}

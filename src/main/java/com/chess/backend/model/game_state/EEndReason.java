package com.chess.backend.model.game_state;

public enum EEndReason {
    CHECKMATE,
    STALEMATE,
    FIFTY_MOVE_RULE,
    INSUFFICIENT_MATERIAL,
    THREEFOLD_REPETITION,
    TIMEOUT
}

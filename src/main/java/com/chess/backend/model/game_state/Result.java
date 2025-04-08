package com.chess.backend.model.game_state;

import java.io.Serializable;

public record Result(EPlayer winner, EEndReason reason) implements Serializable {

    public static Result win(EPlayer winner, EEndReason reason) {
        return new Result(winner, reason);
    }

    public static Result draw(EEndReason reason) {
        return new Result(EPlayer.NONE, reason);
    }

}

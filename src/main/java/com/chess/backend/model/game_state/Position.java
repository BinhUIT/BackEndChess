package com.chess.backend.model.game_state;

import java.io.Serializable;
import org.springframework.lang.Contract;
import com.google.firebase.internal.NonNull;

public record Position(int row, int column) implements Serializable {

    public EPlayer getSquareColor() {
        if ((row + column) % 2 == 0) {
            return EPlayer.WHITE;
        }
        return EPlayer.BLACK;
    }

    @NonNull
    @Contract("_ -> new")
    public Position plus(Direction dir) {
        return new Position(row + dir.getRowDelta(), column + dir.getColumnDelta());
    }
}

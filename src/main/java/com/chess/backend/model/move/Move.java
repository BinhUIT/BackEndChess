package com.chess.backend.model.move;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;
import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public abstract class Move {
    private boolean isCapture = false;
    private EMoveType type;
    private Position fromPos;
    private Position toPos;
    public abstract EMoveType getType();
    public abstract Position getFromPos();
    public abstract Position getToPos();

    public boolean isCapture() {
        return isCapture;
    }

    public void setCapture(boolean capture) {
        this.isCapture = capture;
    }
    public boolean execute(Board board) {
        return false;
    }
    
    public boolean isLegal(Board board) {
        EPlayer player = board.getPiece(getFromPos()).getPlayerColor();
        Board boardCopy = board.copy();
        execute(boardCopy);
        return !boardCopy.isInCheck(player);
    }

}

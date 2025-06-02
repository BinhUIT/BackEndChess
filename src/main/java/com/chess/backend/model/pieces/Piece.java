package com.chess.backend.model.pieces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.Direction;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;
import com.chess.backend.model.move.Move;
import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public abstract class Piece implements Serializable {
    protected boolean hasMoved = false;

    public abstract EPieceType getType();

    public abstract EPlayer getPlayerColor();

    public abstract Piece copy();

    public abstract List<Move> getMoves(Position from, Board board);

    public boolean getHasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    protected List<Position> getMovePositionsInDir(Position from, Board board, Direction dir) {
        List<Position> positions = new ArrayList<>();
        Position pos = from.plus(dir);

        while (Board.isInside(pos)) {
            if (board.isEmpty(pos)) {
                positions.add(pos);
                pos = pos.plus(dir);
                continue;
            }

            Piece piece = board.getPiece(pos);
            if (piece.getPlayerColor() != this.getPlayerColor()) {
                positions.add(pos);
            }

            break;
        }

        return positions;
    }

    protected List<Position> getMovePositionsInDirs(Position from, Board board, Direction[] dirs) {
        List<Position> positions = new ArrayList<>();
        for (Direction dir : dirs) {
            positions.addAll(getMovePositionsInDir(from, board, dir));
        }
        return positions;
    }

    public boolean canCaptureOpponentKing(Position from, Board board) {
        for (Move move : getMoves(from, board)) {
            Piece piece = board.getPiece(move.getToPos());
            if (piece != null && piece.getType() == EPieceType.KING) {
                return true;
            }
        }
        return false;
    }

}

package com.chess.backend.model.pieces;

import java.util.ArrayList;
import java.util.List;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.Direction;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;
import com.chess.backend.model.move.Castle;
import com.chess.backend.model.move.EMoveType;
import com.chess.backend.model.move.Move;
import com.chess.backend.model.move.NormalMove;

public class King extends Piece {
    private static final Direction[] DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST,
            Direction.NORTH_EAST,
            Direction.NORTH_WEST,
            Direction.SOUTH_EAST,
            Direction.SOUTH_WEST
    };

    private final EPlayer color;

    public King(EPlayer color) {
        this.color = color;
    }

    @Override
    public EPieceType getType() {
        return EPieceType.KING;
    }

    @Override
    public EPlayer getPlayerColor() {
        return color;
    }

    @Override
    public Piece copy() {
        King copy = new King(color);
        copy.setHasMoved(getHasMoved());
        return copy;
    }

    private List<Position> getMovePositions(Position from, Board board) {
        List<Position> positions = new ArrayList<>();

        for (Direction dir : DIRECTIONS) {
            Position to = from.plus(dir);
            if (Board.isInside(to) &&
                    (board.isEmpty(to) || board.getPiece(to).getPlayerColor() != color)) {
                positions.add(to);
            }
        }

        return positions;
    }

    private boolean canCastleKingSide(Position from, Board board) {
        if (getHasMoved()) {
            return false;
        }

        Position rookPos = new Position(from.row(), 7);
        Position[] betweenPositions = {
                new Position(from.row(), 5),
                new Position(from.row(), 6)
        };

        return isUnmovedRook(rookPos, board) && allEmpty(betweenPositions, board);
    }

    private boolean canCastleQueenSide(Position from, Board board) {
        if (getHasMoved()) {
            return false;
        }

        Position rookPos = new Position(from.row(), 0);
        Position[] betweenPositions = {
                new Position(from.row(), 1),
                new Position(from.row(), 2),
                new Position(from.row(), 3)
        };

        return isUnmovedRook(rookPos, board) && allEmpty(betweenPositions, board);
    }

    private boolean isUnmovedRook(Position pos, Board board) {
        if (board.isEmpty(pos)) {
            return false;
        }

        Piece piece = board.getPiece(pos);
        return piece.getType() == EPieceType.ROOK && !piece.getHasMoved();
    }

    private boolean allEmpty(Position[] positions, Board board) {
        for (Position pos : positions) {
            if (!board.isEmpty(pos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Move> getMoves(Position from, Board board) {
        List<Move> moves = new ArrayList<>();

        // Normal moves
        for (Position to : getMovePositions(from, board)) {
            moves.add(new NormalMove(from, to));
        }

        // Castling
        if (canCastleKingSide(from, board)) {
            moves.add(new Castle(EMoveType.CASTLE_KS, from));
        }

        if (canCastleQueenSide(from, board)) {
            moves.add(new Castle(EMoveType.CASTLE_QS, from));
        }

        return moves;
    }

    @Override
    public boolean canCaptureOpponentKing(Position from, Board board) {
        for (Position to : getMovePositions(from, board)) {
            Piece piece = board.getPiece(to);
            if (piece != null && piece.getType() == EPieceType.KING) {
                return true;
            }
        }

        return false;
    }
}

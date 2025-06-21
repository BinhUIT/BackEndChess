package com.chess.backend.model.pieces;

import java.util.ArrayList;
import java.util.List;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.Direction;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;
import com.chess.backend.model.move.DoublePawn;
import com.chess.backend.model.move.EnPassant;
import com.chess.backend.model.move.Move;
import com.chess.backend.model.move.NormalMove;
import com.chess.backend.model.move.PawnPromotion;

public class Pawn extends Piece {
    private final EPlayer color;
    private final Direction forward;

    public Pawn(EPlayer color) {
        this.color = color;
        if (color == EPlayer.WHITE) {
            this.forward = Direction.NORTH;
        } else {
            this.forward = Direction.SOUTH;
        }
    }

    @Override
    public EPieceType getType() {
        return EPieceType.PAWN;
    }

    @Override
    public EPlayer getPlayerColor() {
        return color;
    }

    @Override
    public Piece copy() {
        Pawn copy = new Pawn(color);
        copy.setHasMoved(getHasMoved());
        return copy;
    }

    private boolean canMoveTo(Position pos, Board board) {
        return Board.isInside(pos) && board.isEmpty(pos);
    }

    private boolean canCaptureAt(Position pos, Board board) {
        if (!Board.isInside(pos) || board.isEmpty(pos)) {
            return false;
        }

        return board.getPiece(pos).getPlayerColor() != color;
    }

    private List<Move> getPromotionMoves(Position from, Position to) {
        List<Move> moves = new ArrayList<>();
        moves.add(new PawnPromotion(from, to, EPieceType.KNIGHT));
        moves.add(new PawnPromotion(from, to, EPieceType.BISHOP));
        moves.add(new PawnPromotion(from, to, EPieceType.ROOK));
        moves.add(new PawnPromotion(from, to, EPieceType.QUEEN));
        return moves;
    }

    private List<Move> getForwardMoves(Position from, Board board) {
        List<Move> moves = new ArrayList<>();

        Position oneMovePos = from.plus(forward);
        if (canMoveTo(oneMovePos, board)) {
            if (oneMovePos.row() == 0 || oneMovePos.row() == 7) {
                // promotion after moving
                moves.addAll(getPromotionMoves(from, oneMovePos));
            } else {
                moves.add(new NormalMove(from, oneMovePos));
            }

            // two squares forward if not moved
            if (!getHasMoved()) {
                Position twoMovePos = oneMovePos.plus(forward);
                if (canMoveTo(twoMovePos, board)) {
                    moves.add(new DoublePawn(from, twoMovePos));
                }
            }
        }

        return moves;
    }

    private List<Move> getDiagonalMoves(Position from, Board board) {
        List<Move> moves = new ArrayList<>();

        Direction[] dirs = { Direction.WEST, Direction.EAST };
        for (Direction dir : dirs) {
            Position to = from.plus(forward).plus(dir);

            if (to.equals(board.getPawnSkipPosition(color.getOpponent()))) {
                moves.add(new EnPassant(from, to));
            } else if (canCaptureAt(to, board)) {
                if (to.row() == 0 || to.row() == 7) {
                    // promotion after capturing
                    moves.addAll(getPromotionMoves(from, to));
                } else {
                    moves.add(new NormalMove(from, to));
                }
            }
        }

        return moves;
    }

    @Override
    public List<Move> getMoves(Position from, Board board) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(getForwardMoves(from, board));
        moves.addAll(getDiagonalMoves(from, board));
        return moves;
    }

    @Override
    public boolean canCaptureOpponentKing(Position from, Board board) {
        for (Move move : getDiagonalMoves(from, board)) {
            Piece piece = board.getPiece(move.getToPos());
            if (piece != null && piece.getType() == EPieceType.KING) {
                return true;
            }
        }

        return false;
    }
}

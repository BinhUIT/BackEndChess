package com.chess.backend.model.game_state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.ToString;

import com.chess.backend.model.move.EnPassant;
import com.chess.backend.model.pieces.Bishop;
import com.chess.backend.model.pieces.EPieceType;
import com.chess.backend.model.pieces.King;
import com.chess.backend.model.pieces.Knight;
import com.chess.backend.model.pieces.Pawn;
import com.chess.backend.model.pieces.Piece;
import com.chess.backend.model.pieces.Queen;
import com.chess.backend.model.pieces.Rook;

@ToString
public class Board implements Serializable {
    @SerializedName("pieces")
    @Expose
    private final Piece[][] pieces = new Piece[8][8];

    @SerializedName("pawnSkipPositions")
    @Expose
    private final Map<EPlayer, Position> pawnSkipPositions = new HashMap<>();

    public Board() {
        pawnSkipPositions.put(EPlayer.WHITE, null);
        pawnSkipPositions.put(EPlayer.BLACK, null);
    }

    public Piece getPiece(int row, int col) {
        return pieces[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        pieces[row][col] = piece;
    }

    public Piece getPiece(Position pos) {
        return getPiece(pos.row(), pos.column());
    }

    public void setPiece(Position pos, Piece piece) {
        setPiece(pos.row(), pos.column(), piece);
    }

    public Position getPawnSkipPosition(EPlayer player) {
        return pawnSkipPositions.get(player);
    }

    public void setPawnSkipPosition(EPlayer player, Position pos) {
        pawnSkipPositions.put(player, pos);
    }

    public Board initial() {
        Board board = new Board();
        board.addStartPieces();
        return board;
    }

    private void addStartPieces() {
        // Add black pieces
        this.setPiece(0, 0, new Rook(EPlayer.BLACK));
        this.setPiece(0, 1, new Knight(EPlayer.BLACK));
        this.setPiece(0, 2, new Bishop(EPlayer.BLACK));
        this.setPiece(0, 3, new Queen(EPlayer.BLACK));
        this.setPiece(0, 4, new King(EPlayer.BLACK));
        this.setPiece(0, 5, new Bishop(EPlayer.BLACK));
        this.setPiece(0, 6, new Knight(EPlayer.BLACK));
        this.setPiece(0, 7, new Rook(EPlayer.BLACK));

        // Add white pieces
        this.setPiece(7, 0, new Rook(EPlayer.WHITE));
        this.setPiece(7, 1, new Knight(EPlayer.WHITE));
        this.setPiece(7, 2, new Bishop(EPlayer.WHITE));
        this.setPiece(7, 3, new Queen(EPlayer.WHITE));
        this.setPiece(7, 4, new King(EPlayer.WHITE));
        this.setPiece(7, 5, new Bishop(EPlayer.WHITE));
        this.setPiece(7, 6, new Knight(EPlayer.WHITE));
        this.setPiece(7, 7, new Rook(EPlayer.WHITE));

        // Add pawns
        for (int c = 0; c < 8; c++) {
            this.setPiece(1, c, new Pawn(EPlayer.BLACK));
            this.setPiece(6, c, new Pawn(EPlayer.WHITE));
        }
    }

    public static boolean isInside(Position pos) {
        return pos.row() >= 0 && pos.row() < 8 && pos.column() >= 0 && pos.column() < 8;
    }

    public boolean isEmpty(Position pos) {
        return this.getPiece(pos) == null;
    }

    public List<Position> getPiecePositions() {
        List<Position> positions = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                if (!isEmpty(pos)) {
                    positions.add(pos);
                }
            }
        }
        return positions;
    }

    public List<Position> getPiecePositionsFor(EPlayer player) {
        return getPiecePositions().stream()
                .filter(pos -> getPiece(pos).getPlayerColor() == player)
                .collect(Collectors.toList());
    }

    public boolean isInCheck(EPlayer player) {
        return getPiecePositionsFor(player.getOpponent()).stream()
                .anyMatch(pos -> {
                    Piece piece = getPiece(pos);
                    return piece.canCaptureOpponentKing(pos, this);
                });
    }

    public Board copy() {
        Board copy = new Board();
        for (Position pos : getPiecePositions()) {
            copy.setPiece(pos, getPiece(pos).copy());
        }
        return copy;
    }

    // Castle rights methods
    public boolean canCastleKingSide(EPlayer player) {
        if (player == EPlayer.WHITE) {
            return isUnmovedKingAndRook(new Position(7, 4), new Position(7, 7));
        } else if (player == EPlayer.BLACK) {
            return isUnmovedKingAndRook(new Position(0, 4), new Position(0, 7));
        }
        return false;
    }

    public boolean canCastleQueenSide(EPlayer player) {
        if (player == EPlayer.WHITE) {
            return isUnmovedKingAndRook(new Position(7, 4), new Position(7, 0));
        } else if (player == EPlayer.BLACK) {
            return isUnmovedKingAndRook(new Position(0, 4), new Position(0, 0));
        }
        return false;
    }

    private boolean isUnmovedKingAndRook(Position kingPos, Position rookPos) {
        if (isEmpty(kingPos) || isEmpty(rookPos)) {
            return false;
        }

        Piece king = getPiece(kingPos);
        Piece rook = getPiece(rookPos);

        return king.getType() == EPieceType.KING && rook.getType() == EPieceType.ROOK
                && !king.getHasMoved() && !rook.getHasMoved();
    }

    // En passant methods
    public boolean canCaptureEnPassant(EPlayer player) {
        Position skipPos = getPawnSkipPosition(player.getOpponent());

        if (skipPos == null) {
            return false;
        }

        Position[] pawnPositions;
        if (player == EPlayer.WHITE) {
            pawnPositions = new Position[] {
                    skipPos.plus(Direction.SOUTH_WEST),
                    skipPos.plus(Direction.SOUTH_EAST)
            };
        } else {
            pawnPositions = new Position[] {
                    skipPos.plus(Direction.NORTH_WEST),
                    skipPos.plus(Direction.NORTH_EAST)
            };
        }

        return hasPawnInPosition(player, pawnPositions, skipPos);
    }

    private boolean hasPawnInPosition(EPlayer player, Position[] pawnPositions, Position skipPos) {
        for (Position pos : pawnPositions) {
            if (!isInside(pos))
                continue;

            Piece piece = getPiece(pos);
            if (piece == null || piece.getPlayerColor() != player || piece.getType() != EPieceType.PAWN) {
                continue;
            }

            EnPassant move = new EnPassant(pos, skipPos);
            if (move.isLegal(this)) {
                return true;
            }
        }

        return false;
    }

    // Counting methods
    public Counting countPieces() {
        Counting counting = new Counting();
        for (Position pos : getPiecePositions()) {
            Piece piece = getPiece(pos);
            counting.increment(piece.getPlayerColor(), piece.getType());
        }
        return counting;
    }

    public boolean hasInsufficientMaterial() {
        Counting counting = countPieces();
        return isKingVKing(counting) ||
                isKingBishopVKing(counting) ||
                isKingKnightVKing(counting) ||
                isKingBishopVKingBishop(counting);
    }

    private boolean isKingVKing(Counting counting) {
        return counting.getTotalCount() == 2;
    }

    private boolean isKingBishopVKing(Counting counting) {
        return counting.getTotalCount() == 3 &&
                (counting.getWhite(EPieceType.BISHOP) == 1 ||
                        counting.getBlack(EPieceType.BISHOP) == 1);
    }

    private boolean isKingKnightVKing(Counting counting) {
        return counting.getTotalCount() == 3 &&
                (counting.getWhite(EPieceType.KNIGHT) == 1 ||
                        counting.getBlack(EPieceType.KNIGHT) == 1);
    }

    private boolean isKingBishopVKingBishop(Counting counting) {
        if (counting.getTotalCount() != 4) {
            return false;
        }

        if (counting.getWhite(EPieceType.BISHOP) != 1 ||
                counting.getBlack(EPieceType.BISHOP) != 1) {
            return false;
        }

        Position wBishopPos = findPiece(EPlayer.WHITE);
        Position bBishopPos = findPiece(EPlayer.BLACK);

        assert bBishopPos != null;
        assert wBishopPos != null;
        return wBishopPos.getSquareColor() == bBishopPos.getSquareColor();
    }

    private Position findPiece(EPlayer color) {
        for (Position pos : getPiecePositionsFor(color)) {
            if (getPiece(pos).getType() == EPieceType.BISHOP) {
                return pos;
            }
        }

        return null; // Should never happen
    }
}

package com.chess.backend.model.game_state;

import com.chess.backend.model.pieces.Piece;
import com.google.firebase.internal.NonNull;

public class StateString {
    private final StringBuilder sb = new StringBuilder();
    
    public StateString(EPlayer currentPlayer, Board board) {
        addPiecePlacement(board);
        sb.append(' ');
        addCurrentPlayer(currentPlayer);
        sb.append(' ');
        addCastlingRights(board);
        sb.append(' ');
        addEnPassant(board, currentPlayer);
    }
    
    @NonNull
    @Override
    public String toString() {
        return sb.toString();
    }
    
    private static char getPieceChar(Piece piece) {
        char c = switch (piece.getType()) {
            case PAWN -> 'p';
            case KNIGHT -> 'n';
            case BISHOP -> 'b';
            case ROOK -> 'r';
            case QUEEN -> 'q';
            case KING -> 'k';
        };

        if (piece.getPlayerColor() == EPlayer.WHITE) {
            return Character.toUpperCase(c);
        }
        
        return c;
    }
    
    private void addRowData(Board board, int row) {
        int empty = 0;
        
        for (int c = 0; c < 8; c++) {
            if (board.getPiece(row, c) == null) {
                empty++;
                continue;
            }
            
            if (empty > 0) {
                sb.append(empty);
                empty = 0;
            }
            
            sb.append(getPieceChar(board.getPiece(row, c)));
        }
        
        if (empty > 0) {
            sb.append(empty);
        }
    }
    
    private void addPiecePlacement(Board board) {
        for (int r = 0; r < 8; r++) {
            if (r != 0) {
                sb.append('/');
            }
            addRowData(board, r);
        }
    }
    
    private void addCurrentPlayer(EPlayer currentPlayer) {
        if (currentPlayer == EPlayer.WHITE) {
            sb.append('w');
        } else {
            sb.append('b');
        }
    }
    
    private void addCastlingRights(Board board) {
        boolean castleWKS = board.canCastleKingSide(EPlayer.WHITE);
        boolean castleWQS = board.canCastleQueenSide(EPlayer.WHITE);
        boolean castleBKS = board.canCastleKingSide(EPlayer.BLACK);
        boolean castleBQS = board.canCastleQueenSide(EPlayer.BLACK);
        
        if (!(castleWKS || castleWQS || castleBKS || castleBQS)) {
            sb.append('-');
            return;
        }
        
        if (castleWKS) {
            sb.append('K');
        }
        if (castleWQS) {
            sb.append('Q');
        }
        if (castleBKS) {
            sb.append('k');
        }
        if (castleBQS) {
            sb.append('q');
        }
    }
    
    private void addEnPassant(Board board, EPlayer currentPlayer) {
        if (!board.canCaptureEnPassant(currentPlayer)) {
            sb.append('-');
            return;
        }
        
        Position pos = board.getPawnSkipPosition(currentPlayer.getOpponent());
        char file = (char)('a' + pos.column());
        int rank = 8 - pos.row();
        sb.append(file);
        sb.append(rank);
    }
}

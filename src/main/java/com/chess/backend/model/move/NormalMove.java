package com.chess.backend.model.move;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.Position;
import com.chess.backend.model.pieces.EPieceType;
import com.chess.backend.model.pieces.Piece;

public class NormalMove extends Move {
    private final Position fromPos;
    private final Position toPos;
    
    public NormalMove(Position from, Position to) {
        this.fromPos = from;
        this.toPos = to;
    }
    
    @Override
    public EMoveType getType() {
        return EMoveType.NORMAL;
    }
    
    @Override
    public Position getFromPos() {
        return fromPos;
    }
    
    @Override
    public Position getToPos() {
        return toPos;
    }
    
    @Override
    public boolean execute(Board board) {
        Piece piece = board.getPiece(fromPos);
        boolean capture = !board.isEmpty(toPos);
        
        board.setPiece(toPos, piece);
        board.setPiece(fromPos, null);
        piece.setHasMoved(true);
        
        return capture || piece.getType() == EPieceType.PAWN;
    }
}

package com.chess.backend.model.move;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;
import com.chess.backend.model.pieces.Bishop;
import com.chess.backend.model.pieces.EPieceType;
import com.chess.backend.model.pieces.Knight;
import com.chess.backend.model.pieces.Piece;
import com.chess.backend.model.pieces.Queen;
import com.chess.backend.model.pieces.Rook;

public class PawnPromotion extends Move {
    private final Position fromPos;
    private final Position toPos;
    private final EPieceType newType;
    
    public PawnPromotion(Position from, Position to, EPieceType newType) {
        this.fromPos = from;
        this.toPos = to;
        this.newType = newType;
    }
    
    @Override
    public EMoveType getType() {
        return EMoveType.PAWN_PROMOTION;
    }
    
    @Override
    public Position getFromPos() {
        return fromPos;
    }
    
    @Override
    public Position getToPos() {
        return toPos;
    }
    
    private Piece createPromotionPiece(EPlayer color) {
        return switch (newType) {
            case KNIGHT -> new Knight(color);
            case BISHOP -> new Bishop(color);
            case ROOK -> new Rook(color);
            default ->  // QUEEN
                    new Queen(color);
        };
    }
    
    @Override
    public boolean execute(Board board) {
        Piece pawn = board.getPiece(fromPos);
        board.setPiece(fromPos, null);
        
        Piece promotionPiece = createPromotionPiece(pawn.getPlayerColor());
        promotionPiece.setHasMoved(true);
        board.setPiece(toPos, promotionPiece);
        
        return true;
    }
}

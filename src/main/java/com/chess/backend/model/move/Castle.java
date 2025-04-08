package com.chess.backend.model.move;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.Direction;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;

public class Castle extends Move {
    private final EMoveType type;
    private final Position fromPos;
    private final Position toPos;
    private final Direction kingMoveDir;
    private final Position rookFromPos;
    private final Position rookToPos;
    
    public Castle(EMoveType type, Position kingPos) {
        this.type = type;
        this.fromPos = kingPos;
        
        if (type == EMoveType.CASTLE_KS) {
            kingMoveDir = Direction.EAST;
            toPos = new Position(kingPos.row(), 6);
            rookFromPos = new Position(kingPos.row(), 7);
            rookToPos = new Position(kingPos.row(), 5);
        } else {  // CASTLE_QS
            kingMoveDir = Direction.WEST;
            toPos = new Position(kingPos.row(), 2);
            rookFromPos = new Position(kingPos.row(), 0);
            rookToPos = new Position(kingPos.row(), 3);
        }
    }
    
    @Override
    public EMoveType getType() {
        return type;
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
        new NormalMove(fromPos, toPos).execute(board);
        new NormalMove(rookFromPos, rookToPos).execute(board);
        
        return false;
    }
    
    @Override
    public boolean isLegal(Board board) {
        EPlayer player = board.getPiece(fromPos).getPlayerColor();
        
        if (board.isInCheck(player)) {
            return false;
        }
        
        Board copy = board.copy();
        Position kingPosInCopy = fromPos;
        
        for (int i = 0; i < 2; i++) {
            Position nextPos = new Position(kingPosInCopy.row(),
                                         kingPosInCopy.column() + kingMoveDir.getColumnDelta());
            new NormalMove(kingPosInCopy, nextPos).execute(copy);
            kingPosInCopy = nextPos;
            
            if (copy.isInCheck(player)) {
                return false;
            }
        }
        
        return true;
    }
}

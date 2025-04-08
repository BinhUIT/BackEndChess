package com.chess.backend.model.pieces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.chess.backend.model.game_state.Board;
import com.chess.backend.model.game_state.Direction;
import com.chess.backend.model.game_state.EPlayer;
import com.chess.backend.model.game_state.Position;
import com.chess.backend.model.move.Move;
import com.chess.backend.model.move.NormalMove;

public class Bishop extends Piece implements Serializable {
    private static final Direction[] DIRECTIONS = {
            Direction.NORTH_EAST,
            Direction.NORTH_WEST,
            Direction.SOUTH_EAST,
            Direction.SOUTH_WEST
    };
    
    private final EPlayer color;
    
    public Bishop(EPlayer color) {
        this.color = color;
    }
    
    @Override
    public EPieceType getType() {
        return EPieceType.BISHOP;
    }
    
    @Override
    public EPlayer getPlayerColor() {
        return color;
    }
    
    @Override
    public Piece copy() {
        Bishop copy = new Bishop(color);
        copy.setHasMoved(getHasMoved());
        return copy;
    }
    
    @Override
    public List<Move> getMoves(Position from, Board board) {
        List<Move> moves = new ArrayList<>();
        
        List<Position> positions = getMovePositionsInDirs(from, board, DIRECTIONS);
        for (Position to : positions) {
            moves.add(new NormalMove(from, to));
        }
        
        return moves;
    }
}

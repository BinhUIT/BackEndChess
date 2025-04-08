package com.chess.backend.model.game_state;

import java.util.EnumMap;
import java.util.Map;

import com.chess.backend.model.pieces.EPieceType;

public class Counting {
    private final Map<EPieceType, Integer> whiteCount = new EnumMap<>(EPieceType.class);
    private final Map<EPieceType, Integer> blackCount = new EnumMap<>(EPieceType.class);
    private int totalCount = 0;
    
    public Counting() {
        for (EPieceType type : EPieceType.values()) {
            whiteCount.put(type, 0);
            blackCount.put(type, 0);
        }
    }
    
    public void increment(EPlayer color, EPieceType type) {
        if (color == EPlayer.WHITE) {
            whiteCount.put(type, this.getWhite(type) + 1);
        } else if (color == EPlayer.BLACK) {
            blackCount.put(type, this.getBlack(type) + 1);
        }
        totalCount++;
    }

    public int getWhite(EPieceType type) {
        Integer count = whiteCount.get(type);
        return count != null ? count : 0;  // Prevents NullPointerException
    }

    public int getBlack(EPieceType type) {
        Integer count = blackCount.get(type);
        return count != null ? count : 0;  // Prevents NullPointerException
    }
    
    public int getTotalCount() {
        return totalCount;
    }
}

package com.chess.backend.referencemodel;

import com.chess.backend.model.Player;
import com.chess.backend.model.enums.EMatchType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueuedRankPlayer {
    private Player player;
    private Integer playTime;
    private EMatchType matchType;
    private long joinedTime; // Thời điểm tham gia hàng đợi
}
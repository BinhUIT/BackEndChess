package com.chess.backend.request;

import java.io.Serializable;

import com.chess.backend.model.enums.ESocketMessageType;
import com.chess.backend.model.game_state.GameState;
import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class MoveRequest implements Serializable {
    private ESocketMessageType messageType;
    private String currentMatchId;
    private Object gameState;
}

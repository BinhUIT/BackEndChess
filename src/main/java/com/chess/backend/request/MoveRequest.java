package com.chess.backend.request;

import java.io.Serializable;

import com.chess.backend.model.game_state.GameState;
import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MoveRequest implements Serializable {
    private String currentMatchId;
    private GameState gameState;
}

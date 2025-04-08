package com.chess.backend.request;

import com.google.auto.value.AutoValue.Builder;
import com.google.firebase.database.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PlayerUpdateRequest {
    @NotNull
    private String playerName;
}

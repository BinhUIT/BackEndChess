package com.chess.backend.request;

import com.chess.backend.model.enums.EMatchType;
import com.google.firebase.database.annotations.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateMatchRequest {
    @NotNull
    private String playerID;

    private boolean playAsWhite; // nguoi tao muon chon quan trang hay khong
    @NotNull
    private Integer playTime;
    @NotNull
    private EMatchType matchType;
}

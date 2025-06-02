package com.chess.backend.model;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player implements Comparable<Player> {
    private String playerId;
    private String email;
    private String playerName;
    private int matches;
    private int rank;
    private int win;
    private int score;

    public Player GetPlayerInfo() {
        return new Player(this.playerId, "", this.playerName, this.matches, this.rank, this.win, this.score);
    }

    public double getRating() {
        double rating = (double) win / matches;
        return (double) Math.round(rating * 100.0) / 100.0;
    }

    public String getId() {
        return playerId;
    }

    @Override
    public int compareTo(Player b) {
        if (this.score > b.score) {
            return -1;
        }
        if (this.score < b.score) {
            return 1;
        }
        return 0;
    }
}

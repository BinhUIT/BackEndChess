package com.chess.backend.repository;

import java.util.List;

import com.chess.backend.model.Player;

public interface PlayerRepository{
    // Save a player to Firebase
    Player savePlayer(Player player);

    // Update a player
    Player updatePlayer(Player player);

    // Delete a player by ID
    void deleteByPlayerId(String id);

    List<Player> getAllPlayers();
}
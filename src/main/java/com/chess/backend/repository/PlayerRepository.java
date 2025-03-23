package com.chess.backend.repository;

import com.chess.backend.model.Player;

public interface PlayerRepository {
    // Save a player to Firebase
    Player save(Player player);

    // Update a player
    Player update(Player player);

    // Delete a player by ID
    void deleteById(String id);
}
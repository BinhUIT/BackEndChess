package com.chess.backend.service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Player;
import com.chess.backend.repository.PlayerRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

@Service
public class PlayerService {

    @Autowired
    private Firestore firestore;
    @Autowired
    private GetDataService getDataService;

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    public Player GetPlayerById(String playerId) throws InterruptedException, ExecutionException {
        DocumentSnapshot snap = getDataService.GetDataSnapShot("User", playerId); 
        if(snap.exists()) {
            System.out.println("Yes");
            return snap.toObject(Player.class);
        } 
        return null;
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Player updatePlayer(Player player) {
        return playerRepository.update(player);
    }

    public void deletePlayer(String id) {
        playerRepository.deleteById(id);
    }

}

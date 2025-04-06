package com.chess.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Player;
import com.chess.backend.repository.FireBasePlayerRepository;
import com.chess.backend.repository.PlayerRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Service
public class PlayerService {

    @Autowired
    private Firestore firestore;
    @Autowired
    private GetDataService getDataService;

    private final FireBasePlayerRepository playerRepository;

    public PlayerService(FireBasePlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player GetPlayerById(String playerId) throws InterruptedException, ExecutionException {
        DocumentSnapshot snap = getDataService.GetDataSnapShot("User", playerId);
        if (snap.exists()) {
            return snap.toObject(Player.class);
        }
        return null;
    }

    public Player GetPlayerByPlayerName(String playerName) throws InterruptedException, ExecutionException {
        DocumentSnapshot snap = getDataService.GetDataSnapShot("User", playerName);
        if (snap.exists()) {
            return snap.toObject(Player.class);
        }
        return null;
    }

    public Player savePlayer(Player player) {
        return playerRepository.savePlayer(player);
    }

    public Player updatePlayer(Player player) {
        return playerRepository.updatePlayer(player);
    }

    public void deletePlayer(String id) {
        playerRepository.deleteByPlayerId(id);
    }
    public List<Player> GetAllPlayer() throws InterruptedException, ExecutionException {
        List<Player> listPlayer = new ArrayList<>();
        List<QueryDocumentSnapshot> listSnapshots = getDataService.GetAllDocumentSnapshot("User");
        for(QueryDocumentSnapshot snap:listSnapshots) {
            if(snap.exists()) {
                Player p= snap.toObject(Player.class);
                listPlayer.add(p.GetPlayerInfo());
            }
        }
        return listPlayer;
   }
}

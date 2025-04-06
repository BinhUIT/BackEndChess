package com.chess.backend.service;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.backend.model.Player;

import com.chess.backend.repository.FireBasePlayerRepository;
import com.chess.backend.request.PlayerRegisterRequest;
import com.chess.backend.request.PlayerUpdateRequest;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

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

    public Player RegisterPlayer(PlayerRegisterRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
    
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
            .setEmail(request.getEmail())
            .setPassword(request.getPassword());
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
            Map<String,Object> data= new HashMap<>();
            data.put("playerId", userRecord.getUid());
            data.put("email", request.getEmail());
            data.put("playerName",request.getUserName());
            data.put("matches",0); 
            data.put("rank",0);
            data.put("win",0);
            data.put("score",0);
            getDataService.SetData("User", userRecord.getUid(), data);
            return new Player(userRecord.getUid(), request.getEmail(), request.getUserName(),0,0,0,0);
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
  
   public void EditPlayerRank(List<Player> listPlayers) {
        int currentRank=1;
        for(int i=0;i<listPlayers.size()-1;i++) {
            listPlayers.get(i).setRank(currentRank); 
            if(listPlayers.get(i).getScore()!=listPlayers.get(i+1).getScore()) {
                currentRank++;
            }
        } 
        listPlayers.get(listPlayers.size()-1).setRank(currentRank);
   }
   public Player UpdatePlayer(PlayerUpdateRequest request, String playerUID) throws InterruptedException, ExecutionException {
        Player player= GetPlayerById(playerUID);
        if(player==null) {
            return null;
        }
        player.setPlayerName(request.getPlayerName());
        HashMap<String,Object> data = new HashMap<>();
        data.put("playerId",player.getPlayerId());
        data.put("email",player.getEmail());
        data.put("matches",player.getMatches());
        data.put("rank",player.getRank());
        data.put("score",player.getScore());
        data.put("win",player.getWin());
        data.put("playerName",request.getPlayerName());
        getDataService.SetData("User", playerUID, data); 
        return player;
   }
}

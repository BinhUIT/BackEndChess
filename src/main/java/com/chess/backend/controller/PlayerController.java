package com.chess.backend.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.chess.backend.model.Match;
import com.chess.backend.model.Player;
import com.chess.backend.request.PlayerRegisterRequest;
import com.chess.backend.service.FirebaseAuthService;
import com.chess.backend.service.MatchService;
import com.chess.backend.service.PlayerService;
import com.google.firebase.auth.FirebaseAuthException;

@RestController
public class PlayerController { 
    @Autowired
    private PlayerService playerService;
    @Autowired
    private FirebaseAuthService firebaseAuthService;
    @Autowired
    private MatchService matchService;
    @GetMapping("/player/{id}") 
    public ResponseEntity<Player> getPlayerById(@PathVariable("id") String id) {
        Player res;
        try {
            res = playerService.GetPlayerById(id);
            if(res==null) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            } 
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);  
        }
    }

    @PostMapping("/register") 
    public ResponseEntity<Player> Register(@RequestBody PlayerRegisterRequest request) {
        Player res;
        try {
            res=playerService.RegisterPlayer(request);
            return new ResponseEntity<>(res,HttpStatus.OK);
        }
        catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    } 

    @GetMapping("/getMatches") 
    public ResponseEntity<List<Match>> GetMatch(@RequestHeader("Authorization") String tokenString) {
        String token = tokenString.substring(7);
        try {
            String userUID= firebaseAuthService.getUidFromToken(token);
            List<Match> res = matchService.GetMatchesOfPlayer(userUID);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(FirebaseAuthException e){ 
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/allPlayer") 
    public ResponseEntity<List<Player>> GetAllPlayer() {
        try {
            List<Player> listPlayer = playerService.GetAllPlayer();
            return new ResponseEntity<>(listPlayer, HttpStatus.OK);
        } 
        catch(InterruptedException|ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
   

}

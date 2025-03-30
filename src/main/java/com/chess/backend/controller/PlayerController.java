package com.chess.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chess.backend.model.Player;
import com.chess.backend.request.PlayerRegisterRequest;
import com.chess.backend.service.PlayerService;

@RestController
public class PlayerController { 
    @Autowired
    private PlayerService playerService;

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
        catch(Exception e) {
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
   

}

package com.chess.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.chess.backend.model.Player;
import com.chess.backend.request.LoginRequest;
import com.chess.backend.response.LoginResponse;
import com.chess.backend.service.PlayerService;
import com.chess.backend.utils.JwtUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @GetMapping("/player/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable("id") String id) {
        Player res;
        try {
            res = playerService.GetPlayerById(id);
            if (res == null) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @MessageMapping("/player.addPlayer")
    @SendTo("/player/topic")
    public Player addPlayer(@Payload Player player) {
        playerService.savePlayer(player);
        return player;
    }

    @PostMapping("/player/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            //Xác thực UID với Firebase Admin SDK
            UserRecord user = FirebaseAuth.getInstance().getUser(request.getUid());
            String customToken = FirebaseAuth.getInstance()
                .createCustomToken(request.getUid());

            Map<String, String> response = new HashMap<>();
            response.put("token", customToken);
            response.put("uid", user.getUid());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body("Invalid UID");
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

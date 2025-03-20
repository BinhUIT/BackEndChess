package com.chess.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.chess.backend.service.FirebaseAuthService;

@RestController
public class TestController { 
    @Autowired
    private FirebaseAuthService firebaseAuthService;
    @GetMapping("/test/token") 
    public ResponseEntity<String> GetUserUID(@RequestHeader("Authorization") String authHeader) {
        String tokenString = authHeader.substring(7);
        try {
            String userUID= firebaseAuthService.getUidFromToken(tokenString);
            return new ResponseEntity<>(userUID, HttpStatus.OK);
        } 
        catch(Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

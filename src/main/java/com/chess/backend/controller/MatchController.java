package com.chess.backend.controller;

import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.backend.model.Match;
import com.chess.backend.service.MatchService;

@RestController
public class MatchController {
    @Autowired 
    private MatchService matchService;
    @GetMapping("/matches") 
    public ResponseEntity<List<Match>> GetAllMatch() {
        List<Match> listMatch = new ArrayList<>();
        try {
            listMatch= matchService.listMatch();
            return new ResponseEntity<>(listMatch, HttpStatus.OK);
        } 
        catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

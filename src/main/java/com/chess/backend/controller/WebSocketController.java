package com.chess.backend.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class WebSocketController {

    @MessageMapping("/app/chat")
    @SendTo("/topic/messages")
    public String handleChatMessage(String message, Principal principal) {
        String userId = principal.getName();
        return "User [" + userId + "]: " + message;
    }
}
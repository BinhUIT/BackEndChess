package com.chess.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Bỏ "Bearer "
                try {
                    FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    String uid = firebaseToken.getUid();
                    Principal userPrincipal = () -> uid;

                    accessor.setUser(userPrincipal); // Lưu thông tin user vào WebSocket session
                    System.out.println("✅ CONNECT Principal = " + userPrincipal.getName());
                } catch (FirebaseAuthException e) {
                    throw new RuntimeException("Invalid Firebase Token", e);
                }
            }
        }
        return message;
    }
}
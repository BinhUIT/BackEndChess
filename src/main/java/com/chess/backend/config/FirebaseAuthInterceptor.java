package com.chess.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && accessor.getCommand() != null) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Bỏ "Bearer "
                try {
                    FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    
                    // Tạo Authentication object để Spring Security sử dụng
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        firebaseToken.getUid(), // Firebase UID
                        null,
                        null
                    );
                    accessor.setUser(auth); // Lưu thông tin user vào WebSocket session
                } catch (FirebaseAuthException e) {
                    throw new RuntimeException("Invalid Firebase Token", e);
                }
            }
        }
        return message;
    }
}
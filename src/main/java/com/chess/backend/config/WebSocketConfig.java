package com.chess.backend.config;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.chess.backend.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho client subscribe
        registry.enableSimpleBroker("/topic", "/queue", "/player");
        
        // Prefix cho client gửi message đến server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix cho tin nhắn riêng tư (user-specific)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép tất cả origin (nên hạn chế trong production)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(
                        ServerHttpRequest request,
                        WebSocketHandler wsHandler,
                        Map<String, Object> attributes
                    ) {
                        // Lấy token từ query parameter
                        String query=request.getURI().getQuery();

                        if (query == null || !query.contains("token=")) {
                            // Xử lý trường hợp không có token
                            return null; // hoặc trả về anonymous principal
                        }
                        String token = query.split("token=")[1];
                        
                        // Xác thực token và trả về Principal
                        if (JwtUtils.validateToken(token)) {
                            return () -> JwtUtils.getUsernameFromToken(token);
                        }
                        throw new AuthenticationCredentialsNotFoundException("Missing or invalid token");
                    }
                })
                .withSockJS();

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Converter cho JSON
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        messageConverters.add(converter);
        
        return false; // Không thêm converters mặc định
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(64 * 1024); // 64KB
        registry.setSendTimeLimit(10 * 1000); // 10 giây
    }
}
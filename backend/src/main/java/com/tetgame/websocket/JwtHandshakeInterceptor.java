package com.tetgame.websocket;

import com.tetgame.config.JwtProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    public JwtHandshakeInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Extract JWT from query param or header
        String token = extractToken(request);
        
        if (token != null && jwtProvider.validateToken(token)) {
            String username = jwtProvider.getSubject(token);
            attributes.put("username", username);
            attributes.put("token", token);
            return true;
        }

        // If no valid token, reject connection
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Post-handshake handling
    }

    private String extractToken(ServerHttpRequest request) {
        // Try to get token from query parameter first
        String uri = request.getURI().toString();
        if (uri.contains("token=")) {
            int startIdx = uri.indexOf("token=") + 6;
            int endIdx = uri.indexOf("&", startIdx);
            if (endIdx == -1) endIdx = uri.length();
            return uri.substring(startIdx, endIdx);
        }

        // Try to get from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}

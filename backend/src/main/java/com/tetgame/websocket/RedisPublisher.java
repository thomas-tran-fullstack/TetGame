package com.tetgame.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishLobbyUpdate(String message) {
        redisTemplate.convertAndSend("lobby:updates", message);
    }

    public void publishRoomUpdate(String roomId, String message) {
        redisTemplate.convertAndSend("room:" + roomId + ":updates", message);
    }

    public void publishGameUpdate(String roomId, String message) {
        redisTemplate.convertAndSend("game:" + roomId + ":updates", message);
    }

    public void publishGameStarted(String roomId, String message) {
        redisTemplate.convertAndSend("game:" + roomId + ":started", message);
    }

    public void publishGameEnded(String roomId, String message) {
        redisTemplate.convertAndSend("game:" + roomId + ":ended", message);
    }

    public void publishChatMessage(String roomId, String message) {
        redisTemplate.convertAndSend("chat:" + roomId + ":messages", message);
    }

    public void publishRoomSeats(String roomId, String message) {
        redisTemplate.convertAndSend("room:" + roomId + ":seats", message);
    }

    public void publishRoomPlayerList(String roomId, String message) {
        redisTemplate.convertAndSend("room:" + roomId + ":player-list", message);
    }

    public void publishUserPresence(String username, String action) {
        redisTemplate.convertAndSend("presence:updates", username + ":" + action);
    }
}

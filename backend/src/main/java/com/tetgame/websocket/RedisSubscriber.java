package com.tetgame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisSubscriber(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void registerChannels(RedisMessageListenerContainer container) {
        // Subscribe to lobby updates
        container.addMessageListener(this, new PatternTopic("lobby:updates"));
        
        // Subscribe to room updates (pattern for all rooms)
        container.addMessageListener(this, new PatternTopic("room:*:updates"));
        // Subscribe to room seats updates
        container.addMessageListener(this, new PatternTopic("room:*:seats"));
        // Subscribe to room player list updates
        container.addMessageListener(this, new PatternTopic("room:*:player-list"));
        
        // Subscribe to game updates
        container.addMessageListener(this, new PatternTopic("game:*:updates"));
        // Subscribe to new game started/ended channels
        container.addMessageListener(this, new PatternTopic("game:*:started"));
        container.addMessageListener(this, new PatternTopic("game:*:ended"));
        
        // Subscribe to chat messages
        container.addMessageListener(this, new PatternTopic("chat:*:messages"));
        
        // Subscribe to presence updates
        container.addMessageListener(this, new PatternTopic("presence:updates"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());

            if (channel.startsWith("lobby:")) {
                messagingTemplate.convertAndSend("/topic/lobby/updates", payload);
            } else if (channel.startsWith("room:") && channel.endsWith(":updates")) {
                String roomId = extractRoomId(channel, "room:", ":updates");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/updates", payload);
            } else if (channel.startsWith("room:") && channel.endsWith(":seats")) {
                String roomId = extractRoomId(channel, "room:", ":seats");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/seats", payload);
            } else if (channel.startsWith("room:") && channel.endsWith(":player-list")) {
                String roomId = extractRoomId(channel, "room:", ":player-list");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/player-list", payload);
            } else if (channel.startsWith("game:") && channel.endsWith(":updates")) {
                String roomId = extractRoomId(channel, "game:", ":updates");
                messagingTemplate.convertAndSend("/topic/game/" + roomId + "/updates", payload);
            } else if (channel.startsWith("game:") && channel.endsWith(":started")) {
                String roomId = extractRoomId(channel, "game:", ":started");
                messagingTemplate.convertAndSend("/topic/game/" + roomId + "/started", payload);
            } else if (channel.startsWith("game:") && channel.endsWith(":ended")) {
                String roomId = extractRoomId(channel, "game:", ":ended");
                messagingTemplate.convertAndSend("/topic/game/" + roomId + "/ended", payload);
            } else if (channel.startsWith("chat:") && channel.endsWith(":messages")) {
                String roomId = extractRoomId(channel, "chat:", ":messages");
                messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/messages", payload);
            } else if (channel.startsWith("presence:")) {
                messagingTemplate.convertAndSend("/topic/presence/updates", payload);
            }
        } catch (Exception ex) {
            // Log error
        }
    }

    private String extractRoomId(String channel, String prefix, String suffix) {
        int startIdx = channel.indexOf(prefix) + prefix.length();
        int endIdx = channel.indexOf(suffix, startIdx);
        return channel.substring(startIdx, endIdx);
    }
}

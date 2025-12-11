package com.tetgame.websocket;

import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class UserSessionRegistry {

    private final RedisSubscriber redisSubscriber;
    private final RedisMessageListenerContainer container;
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSessionRegistry(RedisSubscriber redisSubscriber, RedisMessageListenerContainer container) {
        this.redisSubscriber = redisSubscriber;
        this.container = container;
        // Register Redis channels
        redisSubscriber.registerChannels(container);
    }

    public void registerUser(String username, String sessionId) {
        sessions.put(username, new UserSession(username, sessionId, System.currentTimeMillis()));
    }

    public void unregisterUser(String username) {
        sessions.remove(username);
    }

    public boolean isUserOnline(String username) {
        return sessions.containsKey(username);
    }

    public UserSession getUserSession(String username) {
        return sessions.get(username);
    }

    public Set<String> getOnlineUsers() {
        return sessions.keySet();
    }

    public int getOnlineUserCount() {
        return sessions.size();
    }

    public void updateLastActivity(String username) {
        UserSession session = sessions.get(username);
        if (session != null) {
            session.setLastActivity(System.currentTimeMillis());
        }
    }

    public static class UserSession {
        private final String username;
        private final String sessionId;
        private long connectedAt;
        private long lastActivity;

        public UserSession(String username, String sessionId, long connectedAt) {
            this.username = username;
            this.sessionId = sessionId;
            this.connectedAt = connectedAt;
            this.lastActivity = connectedAt;
        }

        public String getUsername() { return username; }
        public String getSessionId() { return sessionId; }
        public long getConnectedAt() { return connectedAt; }
        public long getLastActivity() { return lastActivity; }
        public void setLastActivity(long time) { this.lastActivity = time; }
    }
}

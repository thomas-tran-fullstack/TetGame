package com.tetgame.websocket;

import com.tetgame.websocket.MessagePayload;
import com.tetgame.websocket.RedisPublisher;
import com.tetgame.websocket.UserSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import com.tetgame.modules.room.service.RoomService;
import com.tetgame.modules.room.service.RoomStateService;
import com.tetgame.modules.game.tienlen.GameEngine;
import com.tetgame.modules.game.tienlen.GameState;

@Controller
public class WebSocketController {

    private final UserSessionRegistry sessionRegistry;
    private final RedisPublisher redisPublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private final RoomStateService roomStateService;
    private final GameEngine gameEngine;
    
    public WebSocketController(UserSessionRegistry sessionRegistry, RedisPublisher redisPublisher,
                              SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper,
                              RoomService roomService, RoomStateService roomStateService,
                              GameEngine gameEngine) {
        this.sessionRegistry = sessionRegistry;
        this.redisPublisher = redisPublisher;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.roomService = roomService;
        this.roomStateService = roomStateService;
        this.gameEngine = gameEngine;
    }

    // Room actions via WebSocket
    @MessageMapping("/room/{roomId}/join")
    public void handleRoomJoin(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) return;
        try {
            UUID userId = UUID.fromString(username);
            var resp = roomService.joinRoom(UUID.fromString(roomId), userId);
            String json = objectMapper.writeValueAsString(resp);
            redisPublisher.publishRoomUpdate(roomId, json);
            redisPublisher.publishRoomSeats(roomId, json);
            redisPublisher.publishRoomPlayerList(roomId, json);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/updates", resp);
        } catch (Exception ex) {
            // ignore
        }
    }

    @MessageMapping("/room/{roomId}/leave")
    public void handleRoomLeave(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) return;
        try {
            UUID userId = UUID.fromString(username);
            var resp = roomService.leaveRoom(UUID.fromString(roomId), userId);
            String json = objectMapper.writeValueAsString(resp);
            redisPublisher.publishRoomUpdate(roomId, json);
            redisPublisher.publishRoomSeats(roomId, json);
            redisPublisher.publishRoomPlayerList(roomId, json);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/updates", resp);
        } catch (Exception ex) {
            // ignore
        }
    }

    @MessageMapping("/room/{roomId}/ready")
    public void handleRoomReady(@DestinationVariable String roomId, @Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) return;
        try {
            UUID userId = UUID.fromString(username);
            Boolean ready = Boolean.valueOf(String.valueOf(payload.getOrDefault("ready", "true")));
            com.tetgame.modules.room.service.RoomService roomService = org.springframework.web.context.support.WebApplicationContextUtils
                    .getRequiredWebApplicationContext(headerAccessor.getSessionAttributes().get("__APPLICATION_CONTEXT__")!=null?null:null)
                    .getBean(com.tetgame.modules.room.service.RoomService.class);
            var resp = roomService.markReady(UUID.fromString(roomId), userId, ready);
            String json = objectMapper.writeValueAsString(resp);
            redisPublisher.publishRoomUpdate(roomId, json);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/updates", resp);
        } catch (Exception ex) {
            // ignore
        }
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String username = (String) headers.getSessionAttributes().get("username");
        String sessionId = headers.getSessionId();

        if (username != null) {
            sessionRegistry.registerUser(username, sessionId);
            // Broadcast user joined to lobby
            try {
                MessagePayload<Map<String, String>> payload = new MessagePayload<>("lobby.user-online", username,
                        Map.of("username", username, "status", "online"));
                redisPublisher.publishUserPresence(username, "online");
                messagingTemplate.convertAndSend("/topic/lobby/users/online", objectMapper.writeValueAsString(payload));
            } catch (Exception ex) {
                // Log error
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String username = (String) headers.getSessionAttributes().get("username");

        if (username != null) {
            sessionRegistry.unregisterUser(username);
            // Broadcast user left
            try {
                MessagePayload<Map<String, String>> payload = new MessagePayload<>("lobby.user-offline", username,
                        Map.of("username", username, "status", "offline"));
                redisPublisher.publishUserPresence(username, "offline");
                messagingTemplate.convertAndSend("/topic/lobby/users/offline", objectMapper.writeValueAsString(payload));
            } catch (Exception ex) {
                // Log error
            }
        }
    }

    // Ping/pong handler for keeping connection alive on mobile
    @MessageMapping("/ping")
    public void handlePing(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            sessionRegistry.updateLastActivity(username);
            messagingTemplate.convertAndSendToUser(username, "/queue/pong", "pong");
        }
    }

    // Chat message handler (can be room-specific)
    @MessageMapping("/chat/room/{roomId}")
    public void handleChatMessage(@Payload MessagePayload<Map<String, String>> message, 
                                  SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            message.setSender(username);
            try {
                String roomId = (String) message.getData().get("roomId");
                redisPublisher.publishChatMessage(roomId, objectMapper.writeValueAsString(message));
                messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/messages", message);
            } catch (Exception ex) {
                // Log error
            }
        }
    }

    // Get online users in lobby
    @MessageMapping("/lobby/users/list")
    public void handleGetOnlineUsers(SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            messagingTemplate.convertAndSendToUser(username, "/queue/lobby/users",
                    sessionRegistry.getOnlineUsers());
        }
    }

    // Room event: player joined
    @MessageMapping("/room/{roomId}/events/player-joined")
    public void handlePlayerJoined(@Payload MessagePayload<Map<String, Object>> message,
                                   @org.springframework.messaging.handler.annotation.DestinationVariable String roomId,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            message.setSender(username);
            try {
                redisPublisher.publishRoomUpdate(roomId, objectMapper.writeValueAsString(message));
            } catch (Exception ex) {
                // Log error
            }
        }
    }

    // Room event: player left
    @MessageMapping("/room/{roomId}/events/player-left")
    public void handlePlayerLeft(@Payload MessagePayload<Map<String, Object>> message,
                                 @org.springframework.messaging.handler.annotation.DestinationVariable String roomId,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            message.setSender(username);
            try {
                redisPublisher.publishRoomUpdate(roomId, objectMapper.writeValueAsString(message));
            } catch (Exception ex) {
                // Log error
            }
        }
    }

    // Room event: ready status changed
    @MessageMapping("/room/{roomId}/events/ready-status")
    public void handleReadyStatus(@Payload MessagePayload<Map<String, Object>> message,
                                  @org.springframework.messaging.handler.annotation.DestinationVariable String roomId,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            message.setSender(username);
            try {
                redisPublisher.publishRoomUpdate(roomId, objectMapper.writeValueAsString(message));
            } catch (Exception ex) {
                // Log error
            }
        }
    }

    // ============= WEBRTC SIGNALING =============
    /**
     * Handle WebRTC signaling messages from clients and forward to target user or broadcast in room.
     * Expected payload: { type: 'offer'|'answer'|'candidate'|'announce', target?: '<userId>', data: {...} }
     */
    @MessageMapping("/room/{roomId}/webrtc")
    public void handleWebRtcSignaling(@DestinationVariable String roomId,
                                      @Payload Map<String, Object> payload,
                                      SimpMessageHeaderAccessor headerAccessor) {
        String sender = (String) headerAccessor.getSessionAttributes().get("username");
        if (sender == null) return;
        try {
            payload.put("sender", sender);
            String target = (String) payload.get("target");
            if (target != null && !target.isEmpty()) {
                // send only to target user (user queue)
                messagingTemplate.convertAndSendToUser(target, "/queue/room/" + roomId + "/webrtc", payload);
            } else {
                // broadcast to room topic
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/webrtc", payload);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ============= GAME HANDLERS (Tiến Lên) =============

    /**
     * Handle player play (đánh bài)
     * Payload: { "cards": [{ "suit": "SPADES", "rank": "THREE" }, ...] }
     */
    @MessageMapping("/game/{roomId}/play")
    public void handleGamePlay(@DestinationVariable String roomId,
                               @Payload Map<String, Object> payload,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) return;
        try {
            UUID userId = UUID.fromString(username);
            
            // Parse cards from payload
            List<Map<String, String>> cardMaps = (List<Map<String, String>>) payload.get("cards");
            if (cardMaps == null || cardMaps.isEmpty()) return;
            
            List<com.tetgame.modules.game.tienlen.Card> cards = new ArrayList<>();
            for (Map<String, String> cardMap : cardMaps) {
                var suit = com.tetgame.modules.game.tienlen.CardSuit.valueOf(cardMap.get("suit"));
                var rank = com.tetgame.modules.game.tienlen.CardRank.valueOf(cardMap.get("rank"));
                cards.add(new com.tetgame.modules.game.tienlen.Card(suit, rank));
            }

            // Get game state from roomStateService
            GameState state = roomStateService.getGameState(UUID.fromString(roomId));
            if (state == null) return;

            // Validate & execute play
            boolean success = gameEngine.playMove(state, userId, cards);
            if (!success) {
                messagingTemplate.convertAndSendToUser(username, "/queue/game/error", 
                    Map.of("error", "Invalid play"));
                return;
            }

            // Broadcast game state to all players in room
            Map<String, Object> gameStateMsg = new HashMap<>();
            gameStateMsg.put("currentPlayer", state.getCurrentPlayer().toString());
            gameStateMsg.put("currentPile", state.getCurrentPile() != null ? state.getCurrentPile().toString() : null);
            gameStateMsg.put("hands", buildHandsResponse(state)); // Don't expose all cards
            gameStateMsg.put("log", state.getGameLog());
            
            redisPublisher.publishGameUpdate(roomId, objectMapper.writeValueAsString(gameStateMsg));
            messagingTemplate.convertAndSend("/topic/game/" + roomId + "/state", gameStateMsg);

            // Check if game ended
            List<UUID> rankings = gameEngine.checkGameEnd(state);
            if (rankings != null) {
                handleGameEnd(UUID.fromString(roomId), state, rankings);
            } else {
                // Send next player notification
                UUID nextPlayer = state.getCurrentPlayer();
                messagingTemplate.convertAndSend("/topic/game/" + roomId + "/next-turn", 
                    Map.of("playerId", nextPlayer.toString()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle player pass (bỏ)
     */
    @MessageMapping("/game/{roomId}/pass")
    public void handleGamePass(@DestinationVariable String roomId,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) return;
        try {
            UUID userId = UUID.fromString(username);
            GameState state = roomStateService.getGameState(UUID.fromString(roomId));
            if (state == null) return;

            boolean allPassed = gameEngine.pass(state, userId);

            // Broadcast state
            Map<String, Object> gameStateMsg = new HashMap<>();
            gameStateMsg.put("currentPlayer", state.getCurrentPlayer().toString());
            gameStateMsg.put("currentPile", state.getCurrentPile() != null ? state.getCurrentPile().toString() : null);
            gameStateMsg.put("hands", buildHandsResponse(state));
            gameStateMsg.put("log", state.getGameLog());
            gameStateMsg.put("passedThisTurn", state.getPassedThisTurn().stream().map(UUID::toString).toList());
            
            redisPublisher.publishGameUpdate(roomId, objectMapper.writeValueAsString(gameStateMsg));
            messagingTemplate.convertAndSend("/topic/game/" + roomId + "/state", gameStateMsg);

            if (allPassed) {
                messagingTemplate.convertAndSend("/topic/game/" + roomId + "/pile-cleared",
                    Map.of("message", "All players passed. Pile cleared. " + userId + " continues."));
            } else {
                UUID nextPlayer = state.getCurrentPlayer();
                messagingTemplate.convertAndSend("/topic/game/" + roomId + "/next-turn",
                    Map.of("playerId", nextPlayer.toString()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle chat in game room
     */
    @MessageMapping("/game/{roomId}/chat")
    public void handleGameChat(@DestinationVariable String roomId,
                               @Payload Map<String, String> payload,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) return;
        try {
            String message = payload.get("text");
            Map<String, Object> chatMsg = new HashMap<>();
            chatMsg.put("sender", username);
            chatMsg.put("text", message);
            chatMsg.put("timestamp", System.currentTimeMillis());
            
            redisPublisher.publishChatMessage(roomId, objectMapper.writeValueAsString(chatMsg));
            messagingTemplate.convertAndSend("/topic/game/" + roomId + "/chat", chatMsg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ============= HELPER METHODS =============

    private Map<String, Integer> buildHandsResponse(GameState state) {
        Map<String, Integer> handCounts = new HashMap<>();
        for (Map.Entry<UUID, List<com.tetgame.modules.game.tienlen.Card>> entry : state.getHands().entrySet()) {
            handCounts.put(entry.getKey().toString(), entry.getValue().size());
        }
        return handCounts;
    }

    private void handleGameEnd(UUID roomId, GameState state, List<UUID> rankings) {
        try {
            // Build settlement info
            Map<String, Object> endMsg = new HashMap<>();
            List<String> rankingStrs = rankings.stream().map(UUID::toString).toList();
            endMsg.put("rankings", rankingStrs);
            endMsg.put("1st", rankingStrs.get(0));
            endMsg.put("2nd", rankingStrs.size() > 1 ? rankingStrs.get(1) : null);
            endMsg.put("3rd", rankingStrs.size() > 2 ? rankingStrs.get(2) : null);
            endMsg.put("4th", rankingStrs.size() > 3 ? rankingStrs.get(3) : null);
            endMsg.put("log", state.getGameLog());

            // Publish to game end topic
            redisPublisher.publishGameEnded(roomId.toString(), objectMapper.writeValueAsString(endMsg));
            messagingTemplate.convertAndSend("/topic/game/" + roomId + "/ended", endMsg);

            // Settlement will be handled by RoomStateService (after room starts with betLevel)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

# WebSocket & Real-time Communication - Giai Đoạn 3 Hoàn Thành

## Tóm Tắt

Giai đoạn 3 (WebSocket & Real-time Layer) đã hoàn thành. Integrated STOMP over WebSocket với JWT authentication, Redis pub/sub channels, và real-time messaging cho lobby, room, và game updates.

## Architecture

```
Client (Frontend)
    ↓ (WebSocket/STOMP)
Spring WebSocket Endpoint (/ws)
    ↓ (with JWT validation)
JwtHandshakeInterceptor
    ↓
StompEndpointRegistry
    ↓ (STOMP protocol)
MessageBrokerRegistry
    ├─ /topic/** (broadcast channels)
    └─ /queue/** (user-specific channels)
    ↓
Redis Pub/Sub
    ├─ lobby:updates
    ├─ room:*:updates
    ├─ game:*:updates
    ├─ chat:*:messages
    └─ presence:updates
    ↓
RedisSubscriber (listener)
    ↓
WebSocket clients
```

## Files Tạo Mới

### Core WebSocket Components
- `src/main/java/com/tetgame/config/WebSocketConfig.java` - STOMP endpoint config + message broker
- `src/main/java/com/tetgame/websocket/JwtHandshakeInterceptor.java` - JWT validation during handshake
- `src/main/java/com/tetgame/websocket/MessagePayload.java` - Generic message payload class

### Redis Integration
- `src/main/java/com/tetgame/websocket/RedisPublisher.java` - Publish messages to Redis channels
- `src/main/java/com/tetgame/websocket/RedisSubscriber.java` - Subscribe & relay Redis messages to WebSocket
- `src/main/java/com/tetgame/config/RedisListenerConfig.java` - Redis listener container configuration

### Session Management
- `src/main/java/com/tetgame/websocket/UserSessionRegistry.java` - Track online users + last activity

### Message Handling
- `src/main/java/com/tetgame/websocket/WebSocketController.java` - STOMP message handlers

## API Endpoints

### WebSocket Endpoint
```
URL: ws://localhost:8081/ws?token=<JWT_TOKEN>
Protocol: STOMP over WebSocket with SockJS fallback
```

### STOMP Channels

#### Publish (send from client)
- `/app/ping` - Ping for keep-alive (mobile)
- `/app/chat/room/{roomId}` - Send chat message to room
- `/app/lobby/users/list` - Request online users list

#### Subscribe (receive on client)
- `/topic/lobby/updates` - Lobby events (new rooms, etc.)
- `/topic/lobby/users/online` - User came online
- `/topic/lobby/users/offline` - User went offline
- `/topic/room/{roomId}/updates` - Room events (player joined, left, etc.)
- `/topic/game/{roomId}/updates` - Game state updates (turn, cards, etc.)
- `/topic/chat/{roomId}/messages` - Chat messages in room
- `/topic/presence/updates` - User presence changes
- `/queue/pong` - Pong response (user-specific, from /user prefix)
- `/queue/lobby/users` - Online users list (user-specific)

## Connection Flow

### 1. Frontend WebSocket Connection
```javascript
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const token = localStorage.getItem('jwtToken');
const socket = new SockJS(`http://localhost:8081/ws?token=${token}`);
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${token}` },
  (frame) => {
    console.log('WebSocket connected');
    // Subscribe to channels
    stompClient.subscribe('/topic/lobby/updates', (message) => {
      console.log('Lobby update:', JSON.parse(message.body));
    });
  },
  (error) => console.error('WebSocket error:', error)
);
```

### 2. Handshake Process
1. Client opens WebSocket to `/ws?token=<JWT>`
2. `JwtHandshakeInterceptor.beforeHandshake()` validates JWT
3. Extracts username from JWT claims
4. Stores username in session attributes
5. Connection established

### 3. Message Flow
1. Client sends STOMP message to `/app/chat/room/123`
2. `WebSocketController.handleChatMessage()` receives it
3. Adds sender username from session
4. Publishes to Redis: `chat:123:messages`
5. `RedisSubscriber.onMessage()` listens on Redis
6. Relays via STOMP to `/topic/chat/123/messages`
7. All subscribed clients receive message

## Redis Channels

| Channel | Published By | Subscribed By | Purpose |
|---------|--------------|---------------|---------|
| `lobby:updates` | RoomService | RedisSubscriber | New room created, room deleted |
| `room:{roomId}:updates` | RoomService | RedisSubscriber | Player joined, left, ready status |
| `game:{roomId}:updates` | GameEngine | RedisSubscriber | Turn started, cards played, game end |
| `chat:{roomId}:messages` | WebSocketController | RedisSubscriber | User chat message |
| `presence:updates` | WebSocketController | RedisSubscriber | User online/offline |

## Ping/Pong (Mobile Keep-Alive)

Mobile connections may be idle-disconnected. Implement ping/pong:

```javascript
// Frontend
setInterval(() => {
  stompClient.send('/app/ping', {}, 'ping');
}, 30000); // Every 30 seconds

// Backend (already implemented)
@MessageMapping("/ping")
public void handlePing(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
  String username = (String) headerAccessor.getSessionAttributes().get("username");
  messagingTemplate.convertAndSendToUser(username, "/queue/pong", "pong");
  sessionRegistry.updateLastActivity(username);
}

// Frontend receive
stompClient.subscribe('/user/queue/pong', (message) => {
  console.log('Keep-alive pong received');
});
```

## Online Users Tracking

`UserSessionRegistry` maintains concurrent map of online users:
- Register on WebSocket connect
- Unregister on disconnect
- Update last activity on ping
- Query online users on demand

```java
sessionRegistry.registerUser("username", sessionId);
sessionRegistry.getOnlineUsers(); // Returns Set<String>
sessionRegistry.isUserOnline("username"); // Returns boolean
```

## Message Payload Structure

```json
{
  "type": "room.player-joined",
  "sender": "john_doe",
  "timestamp": 1702333815000,
  "data": {
    "roomId": "room-abc-123",
    "playerName": "john_doe",
    "playersCount": 3
  }
}
```

## Configuration

### WebSocket Settings
- Endpoint: `/ws`
- Protocol: STOMP with SockJS fallback
- Allowed origins: `http://localhost:5173`, `https://*.onrender.com`
- Message broker: Simple in-memory (can upgrade to RabbitMQ/Redis for distributed)
- Prefix: `/app` (client publishes), `/topic/`, `/queue/` (client subscribes)

### Redis Integration
- Pub/Sub channels enabled
- Pattern-based subscriptions for dynamic room IDs
- Automatic relay to WebSocket clients

## Error Handling

### Connection Errors
- Invalid JWT → Handshake fails (400 Unauthorized)
- Expired token → Handshake fails
- Network timeout → Auto-reconnect with SockJS fallback

### Message Errors
- Malformed STOMP message → Ignored
- Non-existent room → Error in log (not disconnected)
- Serialization error → Fallback to JSON string

## Performance Considerations

1. **Message Broker**
   - Currently: Simple in-memory
   - Scale: Use RabbitMQ or Redis-backed broker
   
2. **Redis Pub/Sub**
   - Enables cross-server communication
   - Useful for scaled deployment on Render
   
3. **Session Registry**
   - In-memory (OK for single server)
   - For clustered: Use Redis-based registry

4. **Compression**
   - STOMP messages can be gzipped
   - Apply at load balancer level

## Frontend Integration Example

```javascript
// Store
const useWebSocketStore = create((set) => ({
  stompClient: null,
  isConnected: false,
  connect: (token) => {
    const socket = new SockJS(`http://localhost:8081/ws?token=${token}`);
    const stompClient = Stomp.over(socket);
    
    stompClient.connect(
      {},
      () => {
        set({ stompClient, isConnected: true });
        
        // Subscribe to lobby
        stompClient.subscribe('/topic/lobby/updates', (msg) => {
          const data = JSON.parse(msg.body);
          // Update store
        });
      }
    );
  },
  send: (destination, body) => {
    const { stompClient } = get();
    if (stompClient) stompClient.send(destination, {}, JSON.stringify(body));
  }
}));
```

## Testing with Insomnia/WebSocket Client

Since Insomnia doesn't fully support STOMP, use:
- **Spring WebSocket Test Framework**
- **WebSocket CLI tools** (wscat, websocat)
- **Frontend browser console**

Example with wscat:
```bash
npm install -g wscat

wscat -c "ws://localhost:8081/ws?token=YOUR_JWT_TOKEN"

# After connected, send STOMP frame:
CONNECT
accept-version:1.0,1.1,1.2
authorization:Bearer YOUR_TOKEN

^@

# Subscribe
SUBSCRIBE
id:sub-1
destination:/topic/lobby/updates

^@
```

## Known Limitations

1. **Simple Message Broker** - Single server only, doesn't scale to multiple instances
2. **No Encryption** - Use WSS (WebSocket Secure) in production
3. **No Rate Limiting** - Can flood with messages
4. **No Message Persistence** - Messages lost if server restarts

## Next Steps

- Frontend: Implement WebSocket client with SockJS/STOMP
- Implement chat UI with message display
- Implement room events (player join/leave animations)
- Add Redis-backed message broker for scaling
- Security: Enable WSS (TLS) for production

---

**Total Files Added**: 7 files
**Build Status**: ✅ SUCCESS
**Last Updated**: 2025-12-11 15:40 UTC+7


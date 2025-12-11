# 🃏 Tiến Lên Online - Full Stack

Multiplayer Tet card game platform built with Spring Boot + React + WebSocket.

## 📊 Project Status

**Backend (Phase 4-5)**: ✅ 100% COMPLETE
- Room System + WebSocket handlers
- Tiến Lên Game Engine
- Settlement with 5 bet levels
- Real-time game state via Redis Pub/Sub

**Frontend (Phase 6)**: ✅ 100% COMPLETE
- React + Vite + TypeScript
- Login/Register authentication
- Room list + Game board
- Real-time WebSocket integration
- State management with Zustand

**Overall**: 🚀 95% Ready for Deployment

---

## 🗂️ Project Structure

```
c:/Code/
├── backend/                    (Spring Boot 3.x)
│   ├── src/main/java/com/tetgame/
│   │   ├── modules/
│   │   │   ├── auth/
│   │   │   ├── room/
│   │   │   ├── game/tienlen/
│   │   │   ├── user/
│   │   │   └── economy/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   └── RedisConfig.java
│   │   ├── websocket/
│   │   │   ├── WebSocketController.java
│   │   │   ├── RedisPublisher.java
│   │   │   └── RedisSubscriber.java
│   │   └── BackendApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       ├── V1__init.sql
│   │       ├── V2__add_oauth_indexes.sql
│   │       ├── V3__add_rooms_table.sql
│   │       └── V4__add_bet_level.sql
│   ├── pom.xml
│   └── Dockerfile
│
└── frontend/                   (React + Vite)
    ├── src/
    │   ├── pages/
    │   │   ├── Login.tsx
    │   │   └── Register.tsx
    │   ├── components/
    │   │   ├── RoomList.tsx
    │   │   ├── GameBoard.tsx
    │   │   └── *.css
    │   ├── services/
    │   │   ├── api.ts
    │   │   └── websocket.ts
    │   ├── store/
    │   │   └── index.ts
    │   ├── types/
    │   │   └── index.ts
    │   ├── App.tsx
    │   └── main.tsx
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    └── Dockerfile
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (for backend)
- **Node.js 20+** (for frontend)
- **PostgreSQL 14+** (database)
- **Redis 6+** (cache & pub/sub)

### Backend Setup

```bash
cd c:\Code\backend

# Install dependencies (Maven)
mvn clean install

# Configure application.yml
# Update: spring.datasource.url, spring.redis.host, etc.

# Build
mvn -DskipTests clean package

# Run
mvn spring-boot:run
# or
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Backend runs on**: http://localhost:8080

**API Docs**: http://localhost:8080/swagger-ui.html

### Frontend Setup

```bash
cd c:\Code\frontend

# Install dependencies
npm install

# Configure environment
# Create .env.local:
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/ws

# Run dev server
npm run dev
```

**Frontend runs on**: http://localhost:5173

### Development Workflow

**Terminal 1 - Backend**:
```bash
cd c:\Code\backend
mvn spring-boot:run
```

**Terminal 2 - Frontend**:
```bash
cd c:\Code\frontend
npm run dev
```

**Terminal 3 - PostgreSQL & Redis** (if using Docker):
```bash
docker-compose up -d
```

---

## 🎮 Game Flow

### 1. **Authentication**
- User registers with email/username/password
- Or logs in with existing credentials
- Backend returns JWT token + user info
- Frontend stores token in localStorage

### 2. **Room Creation & Joining**
- User creates room with name + bet level (BAN1-BAN5)
- Other users see room in list
- Join room → allocated a seat (position 1-4)
- All players ready → host starts game

### 3. **Game Start**
- GameEngine deals 13 cards per player (52/4)
- Check for instant wins (lã tới trắng)
- If instant win → settle payouts immediately
- Else → start turn-based game

### 4. **Turn Sequence**
```
Player A (Current Turn):
  - Select cards from hand
  - Click "Đánh" to play
  - OR "Bỏ" to pass

Backend validates:
  - Cards in player's hand
  - Beats current pile (if any)
  - Updates GameState

Broadcast:
  - All players see game state
  - Card counts for each player
  - Current pile
  - Whose turn next

Player B turn...
```

### 5. **Game End**
- First player to empty hand wins (1st place)
- Others ranked by remaining cards (2nd, 3rd, 4th)
- Settlement calculated based on bet level:
  - BAN1: 1st +10k, 2nd +5k, 3rd -5k, 4th -10k
  - BAN5: 1st +1m, 2nd +500k, 3rd -500k, 4th -1m
- Wallet deltas applied
- Results shown to all players

---

## 🔗 API Endpoints

### Authentication
```
POST   /api/auth/register          Register new user
POST   /api/auth/login             Login with email/password
POST   /api/auth/refresh           Refresh JWT token
GET    /api/auth/me                Get current user info
```

### Rooms
```
GET    /api/rooms                  List all rooms (paginated)
GET    /api/rooms/{roomId}         Get room details
POST   /api/rooms                  Create new room
POST   /api/rooms/{roomId}/join    Join room
POST   /api/rooms/{roomId}/leave   Leave room
POST   /api/rooms/{roomId}/ready   Mark player ready
```

### WebSocket (STOMP)
```
WS /ws                             WebSocket endpoint

// Subscribe to:
/topic/room/{roomId}/updates       Room status changes
/topic/room/{roomId}/seats         Seat allocations
/topic/room/{roomId}/player-list   Player list updates
/topic/game/{roomId}/state         Real-time game state
/topic/game/{roomId}/started       Game started notification
/topic/game/{roomId}/ended         Game ended + rankings
/topic/game/{roomId}/chat          Chat messages

// Send to:
/app/room/{roomId}/join            Join room
/app/room/{roomId}/leave           Leave room
/app/room/{roomId}/ready           Mark ready
/app/game/{roomId}/play            Play cards
/app/game/{roomId}/pass            Pass turn
/app/game/{roomId}/chat            Send chat
```

---

## 🔐 Authentication

### JWT Flow
```
1. User registers → GET JWT tokens
2. Frontend stores accessToken in localStorage
3. All API requests include: Authorization: Bearer {token}
4. On 401 → Auto-refresh using refreshToken
5. On refresh failure → Logout & redirect to login
```

### Token Storage
```javascript
// After login/register
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);
localStorage.setItem('userId', user.id);
```

---

## 🎯 Bet Levels & Payouts

| Level | Range | 1st | 2nd | 3rd | 4th |
|-------|-------|-----|-----|-----|-----|
| BAN1 | 5k-10k | +10k | +5k | -5k | -10k |
| BAN2 | 10k-20k | +20k | +10k | -10k | -20k |
| BAN3 | 50k-100k | +100k | +50k | -50k | -100k |
| BAN4 | 100k-200k | +200k | +100k | -100k | -200k |
| BAN5 | 500k-1m | +1m | +500k | -500k | -1m |

Ranking determined by:
1. Player with 0 cards left = 1st place
2. Remaining players sorted by card count (fewer = higher rank)
3. Settlement applied based on rank + bet level

---

## 🛠️ Building for Production

### Backend
```bash
cd c:\Code\backend

# Build JAR
mvn clean package

# Output: target/backend-0.0.1-SNAPSHOT.jar

# Docker build
docker build -t tetgame-backend .
```

### Frontend
```bash
cd c:\Code\frontend

# Build static files
npm run build

# Output: dist/

# Docker build
docker build -t tetgame-frontend .
```

### Docker Compose (All Services)
```bash
cd c:\Code

docker-compose up -d
```

Services:
- Frontend: http://localhost (via Nginx)
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432
- Redis: localhost:6379

---

## 🧪 Testing

### Backend Unit Tests
```bash
cd c:\Code\backend

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=GameEngineTest

# With coverage
mvn test jacoco:report
```

### Frontend Tests
```bash
cd c:\Code\frontend

# Run tests (if set up)
npm test

# Lint
npm run lint
```

---

## 📝 Environment Configuration

### Backend (`application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tetgame
    username: postgres
    password: password
  redis:
    host: localhost
    port: 6379
  jpa:
    hibernate:
      ddl-auto: validate
server:
  port: 8080
```

### Frontend (`.env.local`)
```
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/ws
```

### Production
```
VITE_API_URL=https://api.tetgame.com/api
VITE_WS_URL=wss://api.tetgame.com/ws
```

---

## 🚀 Deployment on Render.com

### 1. Create Backend Service
```
Build Command: mvn clean package
Start Command: java -jar target/backend-0.0.1-SNAPSHOT.jar
Environment: Add DB_URL, REDIS_URL, etc.
```

### 2. Create Frontend Service
```
Build Command: npm install && npm run build
Start Command: npm run preview (or Nginx)
Static Site: Deploy `dist/` folder
```

### 3. Set Environment Variables
```
SPRING_DATASOURCE_URL=postgresql://...
SPRING_REDIS_URL=redis://...
VITE_API_URL=https://your-backend.onrender.com/api
VITE_WS_URL=wss://your-backend.onrender.com/ws
```

---

## 📊 Game Statistics

**Implemented Cards**:
- 52 standard playing cards (4 suits × 13 ranks)
- Ranks: 3-10, J, Q, K, A, 2 (values: 3-15)
- Suits: ♠ Spades, ♣ Clubs, ♦ Diamonds, ♥ Hearts

**Hand Types**:
- Single, Pair, Triple
- Straight (5+ consecutive cards)
- Consecutive Pairs (2+ pairs in sequence)
- Four-of-a-Kind
- Bombs (certain consecutive pairs)

**Instant Win Hands** (Lã Tới Trắng):
- 6+ different ranks with ≥2 cards each
- Exactly 4 TWO cards (Lã Tư Quỷ Heo)
- All 13 ranks represented (Lã Sanh Rồng)

---

## 🔥 Key Features

✅ Real-time multiplayer gaming
✅ WebSocket for instant updates
✅ JWT authentication + OAuth2
✅ Bet level system (5 tiers)
✅ Instant win detection
✅ Turn-based game flow
✅ Chat during games
✅ Wallet/settlement system
✅ Game history logging
✅ Rank tracking

---

## 📄 License

MIT License - Open source game platform

---

## 🤝 Contributing

Fork → Branch → Commit → Push → PR

---

## 📞 Support

For issues or questions:
- Backend: Check logs in `target/logs/`
- Frontend: Browser DevTools (F12)
- WebSocket: Check Redis channels

---

**Last Updated**: December 11, 2025  
**Phase**: 4, 5, 6 COMPLETE - Ready for Phase 7 (Advanced Features)

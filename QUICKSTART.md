# 🚀 Quick Start Guide - Tiến Lên Online

## ⚡ Fastest Way to Start (Docker)

### 1. Prerequisites
- Docker & Docker Compose installed
- Git (optional)

### 2. Run Everything
```bash
cd c:\Code
docker-compose up -d
```

Wait ~30 seconds for services to start...

**Access**:
- 🎮 Frontend: http://localhost
- 🔌 Backend API: http://localhost:8080
- 📊 Database: postgres://localhost:5432 (postgres/password)
- 💾 Cache: redis://localhost:6379

### 3. Create First Account
1. Go to http://localhost
2. Click "Đăng Ký" (Register)
3. Fill in email, username, password
4. Login
5. Create room or join existing room
6. Invite friends to play!

---

## 🛠️ Manual Development Setup (Detailed)

### Backend (Spring Boot)

**Requirements**: Java 17+, Maven

```bash
# 1. Clone repo
git clone <repo-url>
cd backend

# 2. Install dependencies
mvn clean install

# 3. Configure database (optional if using Docker Compose)
# Edit: src/main/resources/application.yml
# Update: spring.datasource.url, spring.redis.host

# 4. Start services (if local)
# PostgreSQL: docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 postgres:15
# Redis: docker run --name redis -p 6379:6379 redis:7

# 5. Run backend
mvn spring-boot:run
```

✅ Backend ready at: http://localhost:8080

---

### Frontend (React + Vite)

**Requirements**: Node.js 20+

```bash
# 1. Go to frontend folder
cd c:\Code\frontend

# 2. Install dependencies
npm install

# 3. Create .env.local
# Copy from .env.local.example
cp .env.local.example .env.local

# 4. Start dev server
npm run dev
```

✅ Frontend ready at: http://localhost:5173

---

## 📱 Testing the Game

### Scenario: 2-Player Game

**Player 1**:
1. Register with email: player1@example.com
2. Login
3. Create room named "Ván chơi 1" with BAN1 bet level
4. Share room ID with Player 2

**Player 2**:
1. Register with email: player2@example.com
2. Login  
3. Click "Tham gia" (Join) and paste room ID
4. Click "Sẵn sàng" (Ready)

**Player 1**:
5. See Player 2 joined
6. Click "Sẵn sàng" (Ready)
7. Game starts!

**Gameplay**:
- Cards displayed on screen
- Current pile shown in center
- Click cards to select
- Click "Đánh" to play or "Bỏ" to pass
- Chat in right panel
- First to empty hand wins!

---

## 🔧 Common Issues & Fixes

### "Connection refused" error
**Problem**: Backend not running
**Solution**: 
```bash
cd c:\Code\backend
mvn spring-boot:run
```

### "Cannot find module 'axios'"
**Problem**: Frontend dependencies not installed
**Solution**:
```bash
cd c:\Code\frontend
npm install
```

### "Database connection failed"
**Problem**: PostgreSQL not running
**Solution**:
```bash
docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 postgres:15
```

### "Redis connection failed"
**Problem**: Redis not running
**Solution**:
```bash
docker run --name redis -p 6379:6379 redis:7
```

### "CORS error in browser console"
**Problem**: Frontend & backend not on same origin
**Solution**: Use docker-compose (auto-configured) or update CORS in `SecurityConfig.java`

---

## 📦 Build for Production

### Backend JAR
```bash
cd c:\Code\backend
mvn clean package
# Output: target/backend-0.0.1-SNAPSHOT.jar

# Run JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend Build
```bash
cd c:\Code\frontend
npm run build
# Output: dist/

# Preview
npm run preview
```

### Docker Images
```bash
cd c:\Code
docker-compose build
docker-compose push  # if using Docker Hub registry
```

---

## 🎮 Game Rules Quick Reference

### Ranking
- **1st**: First player to empty hand
- **2nd-4th**: Others sorted by remaining cards (fewer = better)

### Bet Levels
```
BAN1 (5k-10k): 1st +10k, 2nd +5k, 3rd -5k, 4th -10k
BAN2 (10k-20k): 1st +20k, 2nd +10k, 3rd -10k, 4th -20k
BAN3 (50k-100k): 1st +100k, 2nd +50k, 3rd -50k, 4th -100k
BAN4 (100k-200k): 1st +200k, 2nd +100k, 3rd -100k, 4th -200k
BAN5 (500k-1m): 1st +1m, 2nd +500k, 3rd -500k, 4th -1m
```

### Card Ranks
3 < 4 < 5 < 6 < 7 < 8 < 9 < 10 < J < Q < K < A < 2

### Hand Types
- Single (1 card)
- Pair (2 same rank)
- Triple (3 same rank)
- Straight (5+ consecutive)
- Bomb (2+ consecutive pairs)
- etc.

---

## 📊 Technology Stack

**Backend**:
- Spring Boot 3.4.12
- Spring Security (JWT)
- Spring WebSocket
- PostgreSQL 15
- Redis 7
- Flyway Migrations
- Maven

**Frontend**:
- React 19.2.0
- Vite 7.2.7
- TypeScript 5.9.3
- React Router v7
- Zustand (State)
- Axios (HTTP)
- STOMP WebSocket

**Infrastructure**:
- Docker & Docker Compose
- Nginx (Reverse Proxy)
- PostgreSQL
- Redis

---

## 🔗 API Documentation

### Auth Endpoints
```
POST /api/auth/register           - Register new user
POST /api/auth/login              - Login
POST /api/auth/refresh            - Refresh JWT
GET  /api/auth/me                 - Current user
```

### Room Endpoints
```
GET  /api/rooms                   - List rooms
GET  /api/rooms/{roomId}          - Get room
POST /api/rooms                   - Create room
POST /api/rooms/{roomId}/join     - Join room
POST /api/rooms/{roomId}/leave    - Leave room
POST /api/rooms/{roomId}/ready    - Mark ready
```

### WebSocket (STOMP)
```
Send to:
  /app/room/{roomId}/join
  /app/room/{roomId}/leave
  /app/room/{roomId}/ready
  /app/game/{roomId}/play
  /app/game/{roomId}/pass
  /app/game/{roomId}/chat

Subscribe to:
  /topic/room/{roomId}/updates
  /topic/game/{roomId}/state
  /topic/game/{roomId}/started
  /topic/game/{roomId}/ended
  /topic/game/{roomId}/chat
```

---

## 📝 Default Credentials (Development)

After first run, create accounts:
- Email: player1@test.com
- Email: player2@test.com
- Email: player3@test.com
- Email: player4@test.com

Password: any password

---

## 📞 Need Help?

1. **Backend Issues**: Check `target/logs/` or console output
2. **Frontend Issues**: Open DevTools (F12) → Console
3. **Database Issues**: Verify PostgreSQL is running on port 5432
4. **WebSocket Issues**: Check Redis is running on port 6379

---

**Status**: ✅ Fully Functional - Ready to Play!

Last Updated: December 11, 2025

# Phase 6: Frontend React + Vite

## 📋 Project Setup

Frontend đã tạo tại: `c:\Code\frontend` (riêng biệt với backend)

### Structure
```
c:/Code/
├── backend/          (Spring Boot 3.x + Maven)
└── frontend/         (React 19 + Vite 7 + TypeScript)
    ├── src/
    │   ├── pages/
    │   │   ├── Login.tsx
    │   │   └── Register.tsx
    │   ├── components/
    │   │   ├── RoomList.tsx
    │   │   ├── RoomList.css
    │   │   ├── GameBoard.tsx
    │   │   ├── GameBoard.css
    │   │   └── Auth.css
    │   ├── services/
    │   │   ├── api.ts (Axios API client)
    │   │   └── websocket.ts (STOMP WebSocket)
    │   ├── store/
    │   │   └── index.ts (Zustand state management)
    │   ├── types/
    │   │   └── index.ts (TypeScript definitions)
    │   └── App.tsx
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    └── .env.local
```

## 📦 Dependencies Installed

```bash
npm install axios react-router-dom zustand sockjs-client stompjs --save
```

Core packages:
- **React 19.2.1** - UI library
- **Vite 7.2.7** - Build tool
- **TypeScript 5.9.3** - Type safety
- **Axios** - HTTP client for API calls
- **React Router v6** - Navigation
- **Zustand** - State management
- **SockJS** - WebSocket fallback
- **Stompjs** - STOMP protocol for WebSocket

## 🔧 Services Implemented

### 1. **API Service** (`src/services/api.ts`)
- Login/Register
- Get current user
- Room operations (list, create, join, leave, mark ready)
- Automatic token refresh on 401
- Request interceptor for Authorization header

### 2. **WebSocket Service** (`src/services/websocket.ts`)
Manages real-time communication via STOMP over WebSocket:

**Room Operations**:
- `joinRoom(roomId)` - /app/room/{roomId}/join
- `leaveRoom(roomId)` - /app/room/{roomId}/leave
- `markReady(roomId, ready)` - /app/room/{roomId}/ready

**Game Operations**:
- `playCards(roomId, cards)` - /app/game/{roomId}/play
- `pass(roomId)` - /app/game/{roomId}/pass
- `sendChat(roomId, text)` - /app/game/{roomId}/chat

**Subscriptions**:
- Room updates → /topic/room/{roomId}/updates
- Game state → /topic/game/{roomId}/state
- Game started → /topic/game/{roomId}/started
- Game ended → /topic/game/{roomId}/ended
- Next turn → /topic/game/{roomId}/next-turn
- Chat → /topic/game/{roomId}/chat

### 3. **State Management** (`src/store/index.ts`)
Using Zustand for lightweight state:

**Auth Store**:
- `user: User | null`
- `isLoggedIn: boolean`
- `setUser(user)`, `logout()`

**Room Store**:
- `rooms: RoomResponse[]`
- `currentRoom: RoomResponse | null`
- `setRooms()`, `setCurrentRoom()`, `updateRoomSeats()`

**Game Store**:
- `gameState: GameState | null`
- `selectedCards: Card[]`
- `isMyTurn: boolean`
- `setGameState()`, `toggleCardSelection()`, `setIsMyTurn()`

**Chat Store**:
- `messages: Array<{sender, text, timestamp}>`
- `addMessage()`, `clearMessages()`

## 🎨 Components Built

### 1. **Login.tsx**
- Email/password authentication
- Auto WebSocket connection after login
- Redirect to rooms list

### 2. **Register.tsx**
- Email, username, password registration
- Password confirmation validation
- Auto login after registration

### 3. **RoomList.tsx**
- Display all available rooms
- Create new room with name + bet level
- Join room button (disabled if full or playing)
- Modal dialog for room creation
- Bet level colors:
  - BAN1 (5k-10k) - Green
  - BAN2 (10k-20k) - Blue
  - BAN3 (50k-100k) - Orange
  - BAN4 (100k-200k) - Dark Orange
  - BAN5 (500k-1m) - Red

### 4. **GameBoard.tsx**
Core game interface with:

**Left Section - Game Table**:
- Current pile display (cards on table)
- Other players' hand counts
- My hand (clickable cards for selection)
- Play/Pass buttons
- Selected card count display

**Right Section - Chat Panel**:
- Message history (auto-scroll)
- Player name + message
- Input field + Send button

**Features**:
- Real-time game state updates via WebSocket
- Card selection toggle
- Turn indicator
- Auto-detect current player
- Chat messaging

## 🔌 WebSocket Integration

**Connection Flow**:
```
1. User logs in → Get accessToken
2. API returns token
3. WebSocketService.connect(token)
4. STOMP handshake with JWT auth header
5. Subscribe to room/game topics
6. Ready for real-time updates
```

**Message Format**:
```typescript
// Play cards
{
  cards: [
    { suit: 'SPADES', rank: 'THREE' },
    { suit: 'SPADES', rank: 'FIVE' }
  ]
}

// Game state update
{
  currentPlayer: 'uuid-123',
  currentPile: { type: 'SINGLE', cards: [...], primaryRankValue: 3 },
  hands: { 'uuid-1': 13, 'uuid-2': 12, ...},
  passedThisTurn: ['uuid-2']
}
```

## 🌐 Environment Configuration

Create `.env.local` in frontend root:

```env
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/ws
```

For production:
```env
VITE_API_URL=https://yourdomain.com/api
VITE_WS_URL=wss://yourdomain.com/ws
```

## 🚀 Running the Frontend

### Development
```bash
cd c:\Code\frontend
npm run dev
```
Starts on http://localhost:5173

### Build for Production
```bash
npm run build
```
Output: `dist/` folder

### Preview Production Build
```bash
npm run preview
```

## 📝 Type Definitions

All types defined in `src/types/index.ts`:

- **Card**: { suit, rank }
- **CardSuit**: SPADES, CLUBS, DIAMONDS, HEARTS
- **CardRank**: THREE...TEN, JACK, QUEEN, KING, ACE, TWO (3-15)
- **Room**: { id, name, status, betLevel, seats, ... }
- **GameState**: { hands, currentPile, passedThisTurn, gameLog, ... }
- **PlayType**: SINGLE, PAIR, TRIPLE, STRAIGHT, etc.
- **User**: { id, username, email, rank, ... }

## 🎯 Next Steps

Phase 6 Completed:
✅ Auth pages (Login/Register)
✅ Room list with creation & joining
✅ Game board with hand display
✅ WebSocket integration
✅ State management
✅ Real-time chat
✅ Card selection & play

Phase 7 (Optional Enhancements):
- [ ] Leaderboard page
- [ ] User profile page
- [ ] Replay/spectator mode
- [ ] Voice chat (WebRTC)
- [ ] Advanced card validation UI
- [ ] Mobile-responsive design
- [ ] Dark/Light theme toggle

## 🐳 Docker Deployment

### Frontend Dockerfile
```dockerfile
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Run Frontend + Backend Together
```bash
# Terminal 1: Backend
cd c:\Code\backend
mvn spring-boot:run

# Terminal 2: Frontend
cd c:\Code\frontend
npm run dev
```

Access:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- WebSocket: ws://localhost:8080/ws

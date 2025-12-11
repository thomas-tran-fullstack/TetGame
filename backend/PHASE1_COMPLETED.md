# TetGame Backend - Giai Đoạn 1 Hoàn Thành

## Tóm Tắt

Giai đoạn 1 (Backend Foundation) đã hoàn thành. Đã tạo nên cơ sở hạ tầng backend Spring Boot đầy đủ với:
- Database schema (Flyway migrations)
- Entities (User, Wallet, Rank)
- Repositories & Services
- JWT Authentication
- Auth Endpoints (register/login/logout)
- Dockerfile
- Insomnia test collection

## Files Tạo Mới

### Core Entities
- `src/main/java/com/tetgame/modules/user/entity/User.java` - User entity với OAuth fields
- `src/main/java/com/tetgame/modules/user/entity/Wallet.java` - Wallet entity
- `src/main/java/com/tetgame/modules/user/entity/Rank.java` - Rank entity

### Repositories
- `src/main/java/com/tetgame/modules/user/repository/UserRepository.java`
- `src/main/java/com/tetgame/modules/user/repository/WalletRepository.java`
- `src/main/java/com/tetgame/modules/user/repository/RankRepository.java`

### Services
- `src/main/java/com/tetgame/modules/user/service/UserService.java` (interface)
- `src/main/java/com/tetgame/modules/user/service/impl/UserServiceImpl.java`
- `src/main/java/com/tetgame/modules/user/service/WalletService.java` (interface)
- `src/main/java/com/tetgame/modules/user/service/impl/WalletServiceImpl.java`

### Security & Auth
- `src/main/java/com/tetgame/config/JwtProvider.java` - JWT provider using jjwt
- `src/main/java/com/tetgame/config/SecurityConfig.java` - Spring Security config
- `src/main/java/com/tetgame/security/JwtAuthenticationFilter.java` - JWT filter
- `src/main/java/com/tetgame/security/CustomUserDetailsService.java` - UserDetails service
- `src/main/java/com/tetgame/security/CustomUserDetails.java` - Custom UserDetails impl

### Controllers & DTOs
- `src/main/java/com/tetgame/controller/AuthController.java` - Auth API endpoints
- `src/main/java/com/tetgame/controller/UserController.java` - User API endpoints
- `src/main/java/com/tetgame/controller/dto/LoginRequest.java`
- `src/main/java/com/tetgame/controller/dto/LoginResponse.java`
- `src/main/java/com/tetgame/controller/dto/RegisterRequest.java`

### Infra
- `Dockerfile` - Multi-stage Docker build
- `src/main/resources/db/migration/V1__init.sql` - Flyway baseline
- `pom.xml` - Thêm jjwt dependencies
- `application.yml` - Cấu hình với port 8081 để tránh Apache httpd
- `insomnia-collection.json` - Test collection cho Insomnia
- `TEST_INSOMNIA.md` - Hướng dẫn test nhanh

## API Endpoints Đã Tạo

### Auth APIs
| Endpoint | Method | Body | Response |
|----------|--------|------|----------|
| `/api/auth/register` | POST | RegisterRequest | LoginResponse (token + user info) |
| `/api/auth/login` | POST | LoginRequest | LoginResponse (token + user info) |
| `/api/auth/logout` | POST | - | `{"message": "Logged out successfully"}` |

### User APIs (Protected - require JWT header)
| Endpoint | Method | Header | Response |
|----------|--------|--------|----------|
| `/api/user/me` | GET | `Authorization: Bearer {token}` | User info (id, username, fullName, email, avatarUrl, balance) |

## Cấu Hình PostgreSQL & Redis

- **PostgreSQL**: Kết nối tới Render PostgreSQL (dpg-d4ruvmemcj7s73fcq4h0-a.singapore-postgres.render.com:5432)
  - Username: `sa`
  - Password: `23T3a0G0eN2XWFB5JXDwUthZROFcbbqn`
  - Database: `multigame`

- **Redis**: Kết nối tới Redis Labs (redis-15587.c89.us-east-1-3.ec2.cloud.redislabs.com:15587)
  - Username: `default`
  - Password: `TrLQ7eBdK23aX01uB9SHvZxvNNVl3wM3`
  - SSL: disabled (để chuyển sang enabled sau nếu cần)

## Cách Test

### Option 1: Insomnia (Recommended)
1. Mở Insomnia
2. `File` → `Import` → chọn `insomnia-collection.json`
3. Chọn environment `Local` (port 8081)
4. Chạy requests từ collection

### Option 2: cURL
```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"pass123","email":"test@example.com","phone":"0912345678","fullName":"Test","dob":"2000-01-01"}'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"pass123"}'

# Get user info (ganti TOKEN với JWT từ login)
curl -X GET http://localhost:8081/api/user/me \
  -H "Authorization: Bearer TOKEN"
```

## Build & Run

```bash
# Build
cd c:\Code\backend
mvn -DskipTests package

# Run JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar

# Hoặc run với Maven
mvn spring-boot:run
```

App sẽ start trên `http://localhost:8081`

## Giai Đoạn Tiếp Theo (Priority Order)

1. **Giai đoạn 5 - Game Engine: Tiến Lên** (25 tasks)
   - Implement Card/Suit enums, deck generator, game rules
   - TurnManager, GameState, SettlementEngine
   - 40 JUnit tests
   - Estimated: 3-4 tuần

2. **Giai đoạn 3 - WebSocket & Real-time** (15 tasks)
   - STOMP config, JWT handshake interceptor
   - Redis pub/sub channels
   - Estimated: 2 tuần

3. **Giai đoạn 4 - Room System** (20 tasks)
   - Room & RoomPlayer entities
   - Room APIs (create/join/leave/list)
   - Estimated: 2-3 tuần

4. **Giai đoạn 2 - OAuth2** (10 tasks)
   - Google & Facebook OAuth integration
   - Estimated: 1-2 tuần

5. **Frontend** (Giai đoạn 6-9)
   - React-Vite + Tailwind setup
   - Lobby UI, Room Wait UI, In-Game UI
   - Estimated: 4-5 tuần

## Lưu Ý

- **Port 8080** bị Apache httpd sử dụng, thay sang **8081**
- Các warnings về @Builder.Default có thể fix sau (không critical)
- Redis SSL disabled để dễ testing, enable khi deploy production
- JWT expiration: 24 hours
- BCrypt password encoding được dùng

## Next Steps

Người dùng đề xuất: Nên tập trung vào Giai đoạn 5 (Tiến Lên game engine) trước, đây là core logic phức tạp nhất.

Sẽ tạo:
- Card/Suit/Rank enums
- Deck generator & shuffler
- HandValidator (kiểm tra combo bài)
- GameState & TurnManager
- SettlementEngine (tính điểm/tiền theo quy tắc Tiến Lên)
- 40+ JUnit tests

---

**Last Updated**: 2025-12-11 15:30 UTC+7

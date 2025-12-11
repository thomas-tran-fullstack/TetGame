# Test API Backend - Insomnia Quick Guide

## Chuẩn bị

1. **Import Insomnia Collection**
   - Mở Insomnia
   - Chọn `File → Import` (hoặc `Ctrl+Shift+I`)
   - Chọn file `insomnia-collection.json`
   - Chọn `Import`

2. **Cấu hình Environment**
   - Chọn environment `Local` (nếu chưa có, tạo mới)
   - Đặt `base_url = http://localhost:8080` (hoặc URL Render của bạn)

3. **Start Backend Server**
   ```bash
   cd c:\Code\backend
   mvn spring-boot:run
   ```

## Test Flow

### 1. Register (Tạo tài khoản mới)
- **Endpoint**: `POST /api/auth/register`
- **Body mẫu**:
  ```json
  {
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "phone": "0912345678",
    "fullName": "Test User",
    "dob": "2000-01-01"
  }
  ```
- **Response mẫu**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 1733953800000,
    "username": "testuser",
    "avatarUrl": null,
    "balance": 0
  }
  ```
- **Note**: Lưu token để dùng ở bước tiếp theo

### 2. Login (Đăng nhập)
- **Endpoint**: `POST /api/auth/login`
- **Body mẫu**:
  ```json
  {
    "username": "testuser",
    "password": "password123"
  }
  ```
- **Response**: Tương tự như Register
- **Copy token** vào environment `jwt_token` để test endpoint protected

### 3. Get Balance (Test JWT)
- **Endpoint**: `GET /api/wallet/balance`
- **Header yêu cầu**: 
  ```
  Authorization: Bearer <token_từ_login>
  ```
- **Response mẫu**:
  ```json
  {
    "balance": 0
  }
  ```

## Test Cases

| # | Action | Expected Result | Notes |
|---|--------|-----------------|-------|
| 1 | Register user `testuser` | Token + balance 0 | Tạo user mới với wallet = 0 |
| 2 | Login lại `testuser` | Token + balance 0 | Phải trùng token từ register |
| 3 | Gọi `/wallet/balance` với token | Balance: 0 | Phải gửi đúng JWT header |
| 4 | Gọi `/wallet/balance` mà không có token | 401 Unauthorized | Security chặn |
| 5 | Register user bị trùng username | 400/409 Error | Validate username unique |
| 6 | Login sai password | 401/403 Error | Validate password |

## Tips Insomnia

- **Copy response vào environment**:
  1. Chọn tab `Response`
  2. Highlight token từ JSON
  3. Chuột phải → `Set Environment Variable` → chọn `jwt_token`
  
- **Hoặc copy thủ công**:
  1. Copy token từ response
  2. Chọn tab `Environment` (góc trên cùng)
  3. Paste vào `jwt_token`

- **Test secured endpoint**:
  - Tạo request mới: `GET /api/wallet/balance`
  - Header: `Authorization: Bearer {{ jwt_token }}`
  - Chạy request

## Troubleshooting

| Lỗi | Nguyên nhân | Fix |
|-----|-----------|-----|
| `Cannot invoke method on null` | Database không tạo wallet | Kiểm tra `V1__init.sql` đã chạy hay chưa |
| `401 Unauthorized` | Token sai/hết hạn | Login lại để lấy token mới |
| `403 Forbidden` | Endpoint không allow | Kiểm tra SecurityConfig nếu endpoint trong whitelist |
| `Connection refused` | Backend chưa start | Chạy `mvn spring-boot:run` |

## Next Steps

- Thêm endpoint `GET /api/user/me` để lấy info user hiện tại
- Thêm endpoint `POST /api/auth/refresh` để refresh token
- Thêm Rank service để update điểm xếp hạng

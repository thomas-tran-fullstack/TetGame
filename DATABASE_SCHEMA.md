# 📊 Database Schema & Backend Requirements

## User Registration & Login Flow

### 1. User Data Captured During Registration
```
Frontend Form (register.html)
├── Họ và tên (fullName)          → Database (required)
├── Ngày sinh (dateOfBirth)        → Database (required)
├── Email                          → Database (required, unique)
├── Số điện thoại (phoneNumber)    → Database (required)
├── Tên đăng nhập (username)       → Database (required, unique)
└── Mật khẩu (password)            → Database (required, hashed)
```

### 2. Initial User Account Setup
```
When Registration Succeeds:
├── Create user record with all fields
├── Hash password before storing
├── Set balance = 1,000,000 VNĐ    (Mỗi tài khoản mới được 1 triệu)
├── Set rankPoints = 0             (Điểm xếp hạng ban đầu)
└── Set createdAt = current date
```

### 3. Login Response Format
After successful login, API should return:
```json
{
    "token": "JWT_TOKEN_HERE",
    "user": {
        "id": 1,
        "username": "username_value",
        "fullName": "Họ và tên từ đăng ký",
        "email": "email@example.com",
        "phoneNumber": "0123456789",
        "balance": 1000000,
        "rankPoints": 0,
        "avatarUrl": "default.png"
    }
}
```

### 4. Header Display Logic
```
Header Components:
├── Logo: "TetGame" (Tet=Red, Game=Gold)
├── Balance: Lấy từ userData.balance
│   └── Hiển thị: formatCurrency(balance) → "1.000.000 VNĐ"
├── Avatar: First letter of userData.fullName
│   └── Ví dụ: "Nguyễn Văn A" → Avatar = "N"
└── Username: userData.fullName
    └── Ví dụ: Hiển thị "Nguyễn Văn A"
```

---

## 📋 Required Database Tables

### User Table
```sql
CREATE TABLE users (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    username            VARCHAR(255) UNIQUE NOT NULL,
    password            VARCHAR(255) NOT NULL,              -- Hashed
    fullName            VARCHAR(255) NOT NULL,              -- From registration
    email               VARCHAR(255) UNIQUE NOT NULL,
    phoneNumber         VARCHAR(20) NOT NULL,
    dateOfBirth         DATE NOT NULL,
    balance             BIGINT DEFAULT 1000000,             -- Start with 1 million
    rankPoints          INT DEFAULT 0,
    avatarUrl           VARCHAR(255),
    loginMethod         VARCHAR(20),                        -- 'local', 'google', 'facebook'
    isActive            BOOLEAN DEFAULT true,
    createdAt           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastLoginAt         TIMESTAMP
);
```

### Transaction History Table
```sql
CREATE TABLE transactions (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    userId              BIGINT NOT NULL,
    type                VARCHAR(50),                        -- 'game_reward', 'mission', 'daily_login', 'spin'
    amount              BIGINT NOT NULL,
    description         VARCHAR(255),
    createdAt           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(id)
);
```

### Game Result Table
```sql
CREATE TABLE game_results (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    gameId              VARCHAR(50) NOT NULL,               -- 'tienlen', 'bala', etc.
    roomId              VARCHAR(50) NOT NULL,
    userId              BIGINT NOT NULL,
    placement           INT,                                -- 1st, 2nd, 3rd, 4th
    pointsEarned        INT,
    moneyEarned         BIGINT,
    createdAt           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(id)
);
```

---

## 🔌 Required API Endpoints

### Authentication Endpoints

#### 1. Register User
```
POST /api/auth/register
Request:
{
    "fullName": "Nguyễn Văn A",
    "dateOfBirth": "2005-01-15",
    "email": "user@example.com",
    "phoneNumber": "0123456789",
    "username": "user123",
    "password": "securePassword123"
}

Response (Success):
{
    "success": true,
    "message": "Đăng ký thành công",
    "user": {
        "id": 1,
        "username": "user123",
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "balance": 1000000,
        "rankPoints": 0
    }
}

Backend Actions:
✓ Validate input (checked by frontend)
✓ Hash password with bcrypt
✓ Create user with balance = 1,000,000
✓ Return user data
```

#### 2. Login
```
POST /api/auth/login
Request:
{
    "username": "user123",
    "password": "securePassword123"
}

Response (Success):
{
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
        "id": 1,
        "username": "user123",
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "balance": 1000000,
        "rankPoints": 5,
        "avatarUrl": "default.png"
    }
}

Frontend Will:
✓ Save token in localStorage
✓ Save user data with fullName and balance
✓ Display fullName in header
✓ Display balance in header
```

#### 3. Request OTP (Forgot Password)
```
POST /api/auth/request-otp
Request:
{
    "email": "user@example.com"
}

Response:
{
    "success": true,
    "message": "OTP đã được gửi"
}

Backend Actions:
✓ Find user by email
✓ Generate 6-digit OTP
✓ Store OTP with 10-minute expiration
✓ Send OTP via email
```

#### 4. Verify OTP
```
POST /api/auth/verify-otp
Request:
{
    "email": "user@example.com",
    "otp": "123456"
}

Response:
{
    "success": true,
    "message": "OTP verified"
}

Backend Actions:
✓ Check if OTP is correct
✓ Check if OTP is not expired (10 min)
✓ Clear OTP after verification
```

#### 5. Reset Password
```
POST /api/auth/reset-password
Request:
{
    "email": "user@example.com",
    "otp": "123456",
    "newPassword": "newPassword456"
}

Response:
{
    "success": true,
    "message": "Mật khẩu đã được cập nhật"
}

Backend Actions:
✓ Verify OTP is valid and matches email
✓ Hash new password
✓ Update password in database
✓ Clear OTP
```

### User Profile Endpoints

#### Get User Profile
```
GET /api/user/profile
Headers:
Authorization: Bearer <token>

Response:
{
    "id": 1,
    "username": "user123",
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "phoneNumber": "0123456789",
    "dateOfBirth": "2005-01-15",
    "balance": 1500000,
    "rankPoints": 25,
    "avatarUrl": "user_avatar.png",
    "createdAt": "2025-12-13"
}
```

#### Update User Profile
```
PUT /api/user/profile
Headers:
Authorization: Bearer <token>

Request:
{
    "fullName": "Nguyễn Văn A (Updated)",
    "phoneNumber": "0987654321",
    "avatarUrl": "new_avatar.png"
}

Response:
{
    "success": true,
    "user": { updated user data }
}

Rules:
✗ Cannot change email
✓ Can change fullName, phoneNumber, avatar
```

---

## 💾 Data Flow Example

### Scenario: New User Registration → Login → See Header

```
1. User fills registration form
   ├─ Full Name: "Nguyễn Văn A"
   ├─ DOB: "2005-01-15"
   ├─ Email: "user@example.com"
   ├─ Phone: "0123456789"
   ├─ Username: "nguyenvana"
   └─ Password: "SecurePass123"

2. Frontend validates & calls POST /api/auth/register

3. Backend processes:
   ├─ Check if username/email already exist
   ├─ Hash password
   ├─ Create user record:
   │  ├─ fullName: "Nguyễn Văn A"
   │  ├─ balance: 1,000,000 VNĐ  ← Mỗi tài khoản mới được 1 triệu
   │  ├─ rankPoints: 0
   │  └─ other fields from form
   └─ Return user data

4. Frontend stores user data in localStorage

5. User logs in with username & password

6. Backend validates & returns:
   ├─ JWT token
   └─ user: { fullName, balance, rankPoints, ... }

7. Frontend displays in header:
   ├─ Avatar: "N" (from fullName first letter)
   ├─ Username: "Nguyễn Văn A" (fullName)
   └─ Balance: "1.000.000 VNĐ" (formatted balance)
```

---

## 🔐 Security Notes

1. **Password Storage:**
   - Hash password with bcrypt before storing
   - Never store plain-text passwords
   - Use strong salt (minimum 10 rounds)

2. **JWT Tokens:**
   - Generate JWT with secret key
   - Include user ID and minimal info
   - Set expiration (e.g., 24 hours)
   - Refresh tokens for longer sessions

3. **OTP Security:**
   - Generate random 6-digit code
   - Store with email & expiration time
   - Expire after 10 minutes
   - Clear after use or expiration

4. **Data Validation:**
   - Frontend validation (user experience)
   - Backend validation (security)
   - Sanitize all inputs
   - Check authorization on protected endpoints

---

## 📝 Implementation Checklist

### Backend Developer
- [ ] Create User table with fullName, balance fields
- [ ] Implement /api/auth/register endpoint
  - [ ] Validate input
  - [ ] Set balance = 1,000,000 for new users
  - [ ] Hash password
  - [ ] Return user data with fullName
- [ ] Implement /api/auth/login endpoint
  - [ ] Verify credentials
  - [ ] Return JWT token
  - [ ] Include fullName and balance in response
- [ ] Implement /api/auth/request-otp endpoint
- [ ] Implement /api/auth/verify-otp endpoint
- [ ] Implement /api/auth/reset-password endpoint
- [ ] Setup CORS for frontend
- [ ] Test with frontend

### Frontend (Already Done)
- [x] Registration form with validation
- [x] Login form
- [x] Forgot password flow
- [x] Store user data in localStorage
- [x] Display fullName in header
- [x] Display balance in header
- [x] API integration ready

---

## 🧪 Testing

### Manual Test: Registration → Login → Header
```
1. Open /templates/register.html
2. Fill form with:
   - Full Name: "Test User"
   - DOB: "2005-01-15"
   - Email: "test@example.com"
   - Phone: "0123456789"
   - Username: "testuser"
   - Password: "Test123!"
3. Submit → Should see success alert

4. Navigate to /templates/login.html
5. Login with username & password
6. Verify header shows:
   - Avatar: "T" (from "Test User")
   - Username: "Test User" (fullName)
   - Balance: "1.000.000 VNĐ" (formatted)

Result: ✓ PASS if header displays correct fullName and balance
```

---

**Implementation Status**: Frontend ✅ Ready | Backend ⏳ To Be Implemented

The frontend is ready to receive user data with `fullName` and `balance` fields from the backend.

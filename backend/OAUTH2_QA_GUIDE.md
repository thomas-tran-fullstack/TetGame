# OAuth2 Google & Facebook Integration - QA Testing Guide

## Cấu Hình OAuth2 Credentials

### Google OAuth2

1. **Tạo Google Cloud Project**
   - Truy cập [Google Cloud Console](https://console.cloud.google.com/)
   - Tạo project mới (e.g., "tetgame")
   - Bật "Google+ API" trong APIs & Services

2. **Tạo OAuth2 Credentials**
   - Vào `APIs & Services` → `Credentials`
   - Chọn `Create Credentials` → `OAuth 2.0 Client ID`
   - Loại ứng dụng: `Web application`
   - Authorized redirect URIs:
     - `http://localhost:8081/login/oauth2/code/google`
     - `https://your-render-domain.onrender.com/login/oauth2/code/google`
   - Copy `Client ID` và `Client Secret`

3. **Set Environment Variables**
   ```bash
   export GOOGLE_CLIENT_ID=your-google-client-id
   export GOOGLE_CLIENT_SECRET=your-google-secret
   ```

   Hoặc set vào `application.yml`:
   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             google:
               client-id: your-google-client-id
               client-secret: your-google-secret
   ```

### Facebook OAuth2

1. **Tạo Facebook App**
   - Truy cập [Facebook Developers](https://developers.facebook.com/)
   - Tạo app mới (chọn loại "Consumer")
   - Thêm "Facebook Login" product

2. **Cấu Hình Facebook Login**
   - Vào `Settings` → `Basic` → copy `App ID` và `App Secret`
   - Vào `Facebook Login` → `Settings`
   - Valid OAuth Redirect URIs:
     - `http://localhost:8081/login/oauth2/code/facebook`
     - `https://your-render-domain.onrender.com/login/oauth2/code/facebook`

3. **Set Environment Variables**
   ```bash
   export FACEBOOK_CLIENT_ID=your-facebook-app-id
   export FACEBOOK_CLIENT_SECRET=your-facebook-secret
   ```

## Flow Kiểm Thử OAuth2

### Test Flow: Google Login

1. **Start Backend**
   ```bash
   cd c:\Code\backend
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

2. **Trigger Google Login (từ Frontend)**
   - Frontend sẽ gửi user tới: `/oauth2/authorization/google`
   - User sẽ được redirect tới Google login page
   - Sau khi login thành công, backend sẽ redirect về: `/api/auth/oauth/callback?token=XXX&username=YYY&avatar=ZZZ&balance=0`

3. **Frontend Handler**
   ```javascript
   // Sau khi nhận callback từ backend
   const params = new URLSearchParams(window.location.search);
   const token = params.get('token');
   const username = params.get('username');
   const avatar = params.get('avatar');
   
   // Lưu vào localStorage
   localStorage.setItem('jwtToken', token);
   localStorage.setItem('username', username);
   localStorage.setItem('avatar', avatar);
   
   // Redirect tới dashboard
   window.location.href = '/dashboard';
   ```

### Test Flow: Facebook Login

- Tương tự như Google, nhưng sử dụng `/oauth2/authorization/facebook`

## API Endpoints

### 1. Refresh Token
**Endpoint**: `POST /api/auth/refresh`
**Header**: 
```
Authorization: Bearer <current-token>
```
**Response**:
```json
{
  "token": "new-jwt-token",
  "expiresIn": 1733953800000,
  "username": "user@example.com",
  "avatarUrl": "https://...",
  "balance": 1000
}
```

### 2. Validate Token
**Endpoint**: `POST /api/auth/validate-token`
**Header**:
```
Authorization: Bearer <token>
```
**Response**:
```json
{
  "valid": true,
  "username": "user@example.com"
}
```

### 3. OAuth Callback (Info Retrieval)
**Endpoint**: `GET /api/auth/oauth/callback`
**Query Params**:
- `token`: JWT token
- `username`: Username
- `avatar`: Avatar URL (optional)
- `balance`: Current balance (optional)

## Test Cases

| # | Scenario | Steps | Expected Result |
|---|----------|-------|-----------------|
| 1 | Google Login (New User) | Click "Login with Google" → Authorize → Redirect | User created in DB, JWT token returned, balance=0 |
| 2 | Google Login (Existing User) | Same user logs in again | Same user retrieved, new JWT issued |
| 3 | Google + Email Already Exists | User exists with same email but no OAuth | Link OAuth to existing user |
| 4 | Facebook Login (New User) | Click "Login with Facebook" → Authorize → Redirect | User created, JWT token, balance=0 |
| 5 | Facebook Profile Picture | Login with Facebook | Avatar URL extracted from Facebook graph |
| 6 | Refresh Token | Call `/api/auth/refresh` with valid JWT | New JWT token issued |
| 7 | Validate Token | Call `/api/auth/validate-token` | Valid=true if token not expired |
| 8 | Invalid Token | Call `/api/auth/validate-token` with bad token | Valid=false |
| 9 | OAuth Callback Redirect | Complete OAuth flow | Browser redirects to `/oauth/callback?token=...&username=...` |
| 10 | Logout (Client-side) | Delete token from localStorage | JWT invalid on next request |

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| "Invalid client id" | OAuth credentials sai | Kiểm tra environment variables, cấu hình client-id/secret |
| "Redirect URI mismatch" | URI không match cấu hình OAuth app | Thêm exact URI vào OAuth app settings |
| "User profile picture null" | Facebook API không trả picture | Fallback to default avatar, hoặc request `picture.width(200).height(200)` |
| "Email not provided by OAuth" | Email privacy setting ẩn | Bắt buộc email scope hoặc dùng OAuth ID |
| "User not found in token" | Invalid JWT | Xác nhận token được sinh từ username, không từ ID |

## Database Verification

Sau khi OAuth2 login thành công, kiểm tra DB:

```sql
SELECT id, username, email, oauth_provider, oauth_id, avatar_url, created_at 
FROM users 
WHERE oauth_provider IS NOT NULL 
ORDER BY created_at DESC 
LIMIT 5;
```

Expected:
- `oauth_provider`: "google" hoặc "facebook"
- `oauth_id`: ID từ Google (subject) hoặc Facebook
- `avatar_url`: URL ảnh từ OAuth provider
- `password`: NULL (vì dùng OAuth)

## Frontend Implementation

### React Example Login Button

```jsx
import { useNavigate } from 'react-router-dom';

export function LoginButtons() {
  const navigate = useNavigate();
  
  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8081/oauth2/authorization/google';
  };
  
  const handleFacebookLogin = () => {
    window.location.href = 'http://localhost:8081/oauth2/authorization/facebook';
  };

  return (
    <div>
      <button onClick={handleGoogleLogin}>Login with Google</button>
      <button onClick={handleFacebookLogin}>Login with Facebook</button>
    </div>
  );
}

// OAuth Callback Handler
export function OAuthCallback() {
  const navigate = useNavigate();
  
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const username = params.get('username');
    
    if (token) {
      localStorage.setItem('jwtToken', token);
      localStorage.setItem('username', username);
      navigate('/dashboard');
    }
  }, [navigate]);
  
  return <div>Logging in...</div>;
}
```

## Notes

- OAuth2 flow sử dụng **stateless JWT** thay vì session cookies
- User được tự động tạo nếu chưa tồn tại
- Avatar URL được lưu lại để hiển thị trong giao diện
- Refresh token endpoint cho phép update JWT khi sắp hết hạn (24h)
- CSRF protection disabled cho OAuth2 endpoints (trusted OAuth providers)

---

**Last Updated**: 2025-12-11 15:35 UTC+7

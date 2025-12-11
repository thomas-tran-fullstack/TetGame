# OAuth2 Google & Facebook Integration - Giai Đoạn 2 Hoàn Thành

## Tóm Tắt

Giai đoạn 2 (OAuth2 Google/Facebook) đã hoàn thành. Integrated OAuth2 login cho cả Google và Facebook với auto-account creation và JWT token generation.

## Files Tạo Mới

### OAuth2 Services
- `src/main/java/com/tetgame/modules/auth/oauth2/GoogleOAuth2Service.java` - Xử lý Google callback
- `src/main/java/com/tetgame/modules/auth/oauth2/FacebookOAuth2Service.java` - Xử lý Facebook callback

### Config
- `src/main/java/com/tetgame/config/OAuth2SuccessHandler.java` - Success handler với JWT generation

### Controllers
- `src/main/java/com/tetgame/controller/TokenController.java` - Token refresh/validate endpoints

### Security
- Cập nhật `src/main/java/com/tetgame/config/SecurityConfig.java` - Thêm OAuth2 config

### DB Migrations
- `src/main/resources/db/migration/V2__add_oauth_indexes.sql` - Indexes cho OAuth fields

### Docs & Config
- `OAUTH2_QA_GUIDE.md` - QA testing guide với setup instructions
- `pom.xml` - Thêm `spring-boot-starter-oauth2-client` dependency
- `application.yml` - Cấu hình OAuth2 Google/Facebook endpoints

## Tính Năng

### 1. Google OAuth2 Login
- User redirected tới Google login page
- Google returns user info (email, name, picture, subject ID)
- Backend tự động tạo User account hoặc link tới existing email
- JWT token sinh và return cho frontend

### 2. Facebook OAuth2 Login
- Tương tự như Google
- Extract avatar từ Facebook graph API
- Auto-generate username từ email + "fb_" + timestamp

### 3. Account Linking
- Nếu email đã tồn tại nhưng chưa có OAuth:
  - Link OAuth provider ID vào existing user
  - Update avatar URL
  - Không override password

### 4. Token Management
- **Refresh Token** endpoint (`POST /api/auth/refresh`)
  - Require valid JWT in Authorization header
  - Return new JWT token
  - Useful khi token sắp hết hạn (24h)

- **Validate Token** endpoint (`POST /api/auth/validate-token`)
  - Check token validity
  - Return username nếu valid

## API Endpoints

### OAuth2 Endpoints
| Endpoint | Purpose |
|----------|---------|
| `/oauth2/authorization/google` | Redirect tới Google login |
| `/oauth2/authorization/facebook` | Redirect tới Facebook login |
| `/login/oauth2/code/google` | Google callback (handled by Spring) |
| `/login/oauth2/code/facebook` | Facebook callback (handled by Spring) |

### Token Endpoints
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/auth/refresh` | POST | Refresh JWT token |
| `/api/auth/validate-token` | POST | Validate JWT |
| `/api/auth/oauth/callback` | GET | Return OAuth callback info as JSON |

## OAuth Flow Diagram

```
1. User clicks "Login with Google"
   ↓
2. Browser redirected to: /oauth2/authorization/google
   ↓
3. Spring Security redirects to: https://accounts.google.com/o/oauth2/v2/auth
   ↓
4. User logs in và authorizes
   ↓
5. Google redirects to: /login/oauth2/code/google?code=XXX&state=YYY
   ↓
6. Spring exchanges code for access token
   ↓
7. Spring fetches user info từ Google API
   ↓
8. OAuth2SuccessHandler processes:
   - Call GoogleOAuth2Service.processGoogleUser()
   - Create or link user
   - Generate JWT token
   ↓
9. Redirect to: /api/auth/oauth/callback?token=XXX&username=YYY&avatar=ZZZ&balance=0
   ↓
10. Frontend stores token và redirect tới dashboard
```

## Environment Variables

Cần set OAuth credentials (hoặc sẽ dùng placeholders):

```bash
# Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-secret

# Facebook
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-secret
```

Hoặc set vào `application.yml` trực tiếp.

## Database Schema Changes

Thêm indexes cho performance:
- `idx_users_email` - Query user by email
- `idx_users_username` - Query user by username
- `idx_users_oauth_provider_id` - Find user by OAuth provider + ID
- `idx_wallets_user_id` - Quick wallet lookup
- `idx_ranks_user_id` - Quick rank lookup

## Frontend Integration Steps

1. **Install Insomnia** và import collection mới (có refresh/validate endpoints)

2. **Add Login Buttons**
   ```jsx
   <button onClick={() => {
     window.location.href = 'http://localhost:8081/oauth2/authorization/google';
   }}>
     Login with Google
   </button>
   ```

3. **Handle OAuth Callback**
   ```jsx
   useEffect(() => {
     const params = new URLSearchParams(window.location.search);
     const token = params.get('token');
     if (token) {
       localStorage.setItem('jwtToken', token);
       navigate('/dashboard');
     }
   }, []);
   ```

4. **Add Refresh Token Logic** (optional)
   ```jsx
   // Before token expires (24h), call:
   const response = await fetch('/api/auth/refresh', {
     method: 'POST',
     headers: {
       'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
     }
   });
   const newToken = (await response.json()).token;
   localStorage.setItem('jwtToken', newToken);
   ```

## QA Testing Checklist

- [ ] Register new account (traditional username/password)
- [ ] Login with same account
- [ ] Google OAuth login with new Gmail
- [ ] Google OAuth login with existing Gmail
- [ ] Facebook OAuth login with new account
- [ ] Facebook OAuth with picture URL
- [ ] Refresh token endpoint
- [ ] Validate token endpoint
- [ ] Check DB: users table có oauth_provider, oauth_id fields
- [ ] Check avatar URL được lưu và hiển thị

## Known Issues & Limitations

1. **Redirect URI mismatch** - Phải match chính xác trong OAuth app config
2. **Email privacy** - Nếu user hide email trên OAuth provider, có thể không lấy được email
3. **Avatar expiry** - Facebook avatar URLs có thể expire, cần refresh periodically
4. **No password reset** - OAuth users không có password, dùng provider's forgot password

## Next Steps

- Frontend: Tạo OAuth login buttons + callback handler
- Setup Google Cloud + Facebook app credentials
- Test OAuth flows end-to-end
- Deploy tới Render (update redirect URIs)

---

**Total Files Added/Modified**: 8 files
**Build Status**: ✅ SUCCESS
**Last Updated**: 2025-12-11 15:36 UTC+7


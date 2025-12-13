# TetGame Frontend - Cấu Trúc và Hướng Dẫn

## 📁 Cấu Trúc Thư Mục

```
backend/src/main/resources/static/
├── css/
│   ├── global.css              # Stylesheet toàn cụm (biến CSS, layout, responsive)
│   ├── index.css               # Stylesheet cho trang chủ (index.html)
│   ├── auth.css                # Stylesheet cho các trang auth (login, register, forgot-password)
│   ├── header.css              # Stylesheet cho header component
│   ├── home.css                # Stylesheet cho trang home
│   └── register.css            # Stylesheet bổ sung cho register (password strength)
│
├── js/
│   ├── app.js                  # Utilities chung, API helpers, storage helpers
│   ├── validation.js           # Hàm validate dữ liệu input
│   ├── auth.js                 # Logic cho login/logout
│   ├── register.js             # Logic cho trang đăng ký
│   ├── forgot-password.js      # Logic cho trang quên mật khẩu
│   └── home.js                 # Logic cho trang home (game selection)
│
└── templates/
    ├── index.html              # Trang welcome/landing
    ├── login.html              # Trang đăng nhập
    ├── register.html           # Trang đăng ký
    ├── forgot-password.html    # Trang quên mật khẩu
    ├── home.html               # Trang chủ (sau khi đăng nhập)
    └── ... (các trang khác sẽ được thêm)
```

## 🎨 Thiết Kế Màu Sắc

### Color Variables (global.css)
```css
--color-primary: #DC143C;           /* Crimson Red */
--color-primary-dark: #8B0000;      /* Dark Red */
--color-secondary: #1a1a1a;         /* Very Dark Gray */
--color-secondary-light: #2a2a2a;   /* Dark Gray */
--color-accent-gold: #FFD700;       /* Gold */
--color-success: #28a745;           /* Green */
--color-error: #dc3545;             /* Red */
--color-warning: #ffc107;           /* Yellow */
--color-text-light: #f0f0f0;        /* Light Gray */
--color-text-dark: #1a1a1a;         /* Dark Text */
--color-border: #3a3a3a;            /* Border Color */
```

## 📄 Trang HTML và Chức Năng

### 1. **index.html** - Trang Welcome
- Tiêu đề: "Xin Chào"
- Mô tả: "Code bởi Thomas Trần"
- Button: "Đăng Nhập" → `/templates/login.html`
- Hiệu ứng: Fade-in animation

### 2. **login.html** - Trang Đăng Nhập
- Fields: Tên đăng nhập, Mật khẩu
- Links: 
  - "Quên mật khẩu?" → `/templates/forgot-password.html`
  - "Tạo tài khoản" → `/templates/register.html`
- Back button (góc trái trên)
- API: POST `/api/auth/login`

### 3. **register.html** - Trang Đăng Ký
- Fields:
  - Họ và tên
  - Ngày sinh (validation: ≥16 tuổi)
  - Email (validation: @, dấu chấm)
  - Số điện thoại (validation: 10-11 số)
  - Tên đăng nhập (validation: không dấu, không số đầu)
  - Mật khẩu (với password strength indicator)
  - Xác nhận mật khẩu
- Links: "Đăng nhập" → `/templates/login.html`
- Back button
- API: POST `/api/auth/register`

### 4. **forgot-password.html** - Trang Quên Mật Khẩu
- Step 1: Nhập email → gửi OTP
- Step 2: Xác minh OTP (6 chữ số, 10 phút)
- Step 3: Đặt lại mật khẩu mới
- Back button
- API: POST `/api/auth/request-otp`, `/api/auth/verify-otp`, `/api/auth/reset-password`

### 5. **home.html** - Trang Chủ (Sau Đăng Nhập)
- **Header:**
  - Logo "TetGame" (Tet đỏ, Game vàng, hiệu ứng glowing)
  - Balance box (hiển thị tiền + icon xu)
  - Avatar + Username (clickable → profile)
  
- **Content:**
  - Game selector carousel
    - Previous/Next buttons (< và >)
    - Game logo + name
    - Support keyboard (←/→), touch swipe
  
- **Games:**
  - Tiến Lên (active) → `/templates/tienlen.html`
  - Ba Lá, Bài Binh, Xì Dách, Bầu Cua, Lô Tô (coming-soon)
  
- **Play Button:** Màu xanh, kích hoạt game hoặc show coming-soon message
- Require authentication

## 🔧 JavaScript Modules

### **app.js** - Utilities Chung
- `goBack()` - Quay lại trang trước
- `navigateTo(path)` - Navigate đến URL
- `saveUserData(userData)` / `getUserData()` - Local storage
- `saveAuthToken(token)` / `getAuthToken()` - Token management
- `isAuthenticated()` - Check auth status
- `apiGet(url)`, `apiPost(url, data)`, `apiPut(url, data)` - API helpers
- `debounce()`, `throttle()` - Utility functions
- `StorageWithExpiry` - Local storage với expiration

### **validation.js** - Validation Functions
- `isValidEmail(email)` - Email validation
- `isValidPhone(phone)` - Phone (10-11 digits)
- `isValidUsername(username)` - No spaces/special chars, no starting number
- `checkPasswordStrengthLevel(password)` - Returns {level, score}
- `isAtLeast16(dobString)` - Age validation
- `showAlert(message, type, containerId, duration)` - Alert UI
- `clearAlerts(containerId)` - Clear all alerts
- `passwordsMatch(password, confirmPassword)` - Password match check
- `formatCurrency(amount)` - Format number as VNĐ
- `isValidFullName(name)` - Full name validation

### **auth.js** - Authentication Logic
- `handleLogin(event)` - Login form handler
- `loadUserDataToHeader()` - Update header with user info
- `verifyAuth()` - Check authentication on protected pages
- Auto-load user data on page load

### **register.js** - Registration Logic
- `checkPasswordStrength()` - Real-time password strength check
- `validateConfirmPassword()` - Real-time password match validation
- `handleRegister(event)` - Register form handler

### **forgot-password.js** - Password Reset Logic
- `handleEmailSubmit(event)` - Step 1: Email verification
- `handleOTPSubmit(event)` - Step 2: OTP verification
- `handleResetPassword(event)` - Step 3: New password
- `backToEmail()` - Back to step 1
- Multi-step form management

### **home.js** - Home Page Logic
- Game carousel management
- `previousGame()` / `nextGame()` - Navigation
- `playGame()` - Play selected game
- Keyboard support (←/→/Enter)
- Touch/swipe support
- Authentication verification

## 🎯 CSS Classes

### Global Classes
- `.btn` - Button base
- `.btn-primary` / `.btn-secondary` / `.btn-success` - Button variants
- `.btn-lg` / `.btn-sm` - Button sizes
- `.btn-block` - Full-width button

- `.form-container` - Form wrapper
- `.form-group` - Form field group
- `.form-control` - Input field
- `.alert` - Alert box
- `.alert-success` / `.alert-error` / `.alert-warning` / `.alert-info` - Alert types

- `.header` - Header component
- `.back-button` - Back button (fixed position)
- `.container` - Max-width container
- `.full-screen` - Full viewport height/width

### Utility Classes
- `.text-center` / `.text-right` - Text alignment
- `.text-muted` - Muted text color
- `.mt-1/2/3/4` - Margin-top
- `.mb-1/2/3/4` - Margin-bottom
- `.p-1/2/3/4` - Padding
- `.hidden` - Display none
- `.d-flex` / `.flex-column` - Flexbox utilities

## 📱 Responsive Design

### Breakpoints
- **Desktop**: >= 1024px
- **Tablet**: 768px - 1023px
- **Mobile**: 480px - 767px
- **Small Mobile**: < 480px

### Mobile Optimizations
- Stack layout vertically
- Larger touch targets (44px buttons)
- Simplified header layout
- Full-width forms
- Touch-friendly carousel

## 🔐 Authentication Flow

1. **Unauthenticated User:**
   - Lands on `index.html`
   - Clicks "Đăng Nhập" → `login.html`
   - OR "Tạo tài khoản" → `register.html`

2. **Login Process:**
   - Enter username + password
   - POST `/api/auth/login`
   - On success: Save token + user data → Redirect to `home.html`
   - On error: Show error alert

3. **Registration Process:**
   - Fill all fields with validation
   - POST `/api/auth/register`
   - On success: Show success message → Redirect to `login.html`
   - On error: Show error alerts

4. **Forgot Password:**
   - Enter email → POST `/api/auth/request-otp`
   - Enter OTP → POST `/api/auth/verify-otp`
   - Enter new password → POST `/api/auth/reset-password`
   - On success: Redirect to `login.html`

5. **Protected Pages:**
   - Check `isAuthenticated()` on load
   - If not authenticated: Redirect to `login.html`
   - Load user data from localStorage

## 🚀 API Endpoints Required

```
POST /api/auth/login
  Request: { username, password }
  Response: { token, user: { id, fullName, balance, ... } }

POST /api/auth/register
  Request: { fullName, dateOfBirth, email, phoneNumber, username, password }
  Response: { success: true, message: "..." }

POST /api/auth/request-otp
  Request: { email }
  Response: { success: true, message: "..." }

POST /api/auth/verify-otp
  Request: { email, otp }
  Response: { success: true }

POST /api/auth/reset-password
  Request: { email, otp, newPassword }
  Response: { success: true, message: "..." }

GET /api/user/profile (with auth token)
  Response: { id, fullName, email, balance, ... }
```

## 🔄 Local Storage Keys

- `authToken` - JWT token for authentication
- `userData` - User information (name, balance, etc.)

## 📝 Notes

- All pages have fade-in animations
- All buttons have hover effects and active states
- Error boxes are red, success boxes are green
- Password strength indicator updates in real-time
- All forms validate on submit and show specific error messages
- Mobile menu and responsive layouts implemented
- Touch swipe support for game carousel
- Keyboard shortcuts (arrow keys, enter)

## 🛠️ Development Tips

1. **Testing Forms:**
   - Use browser dev tools to inspect localStorage
   - Network tab to monitor API calls
   - Console for debug logs

2. **Styling New Pages:**
   - Import global.css for base styles
   - Use CSS variables for colors
   - Follow existing naming conventions
   - Add media queries for mobile

3. **Adding New Pages:**
   - Create `.html` in `/templates`
   - Create associated `.css` in `/css`
   - Create associated `.js` in `/js` if needed
   - Include back-button if not index
   - Import global.css first

4. **Form Validation:**
   - Call validation functions before submit
   - Show specific error messages
   - Use showAlert() for user feedback
   - Disable buttons during submission

---

**Version**: 1.0.0  
**Last Updated**: December 2025

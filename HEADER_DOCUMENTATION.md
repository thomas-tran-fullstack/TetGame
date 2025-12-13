# 📝 Header Component - Updated Documentation

## Header Structure & Data Flow

### Visual Layout
```
┌─────────────────────────────────────────────────────┐
│  TetGame Logo    |    Balance Box    |  Avatar  Name │
│  (Tet/Game)      |  + Coin Icon      |  + User Info │
└─────────────────────────────────────────────────────┘
```

### Header Components Breakdown

#### 1. Logo (Left Side)
```
Display: "TetGame"
├─ "Tet" = Red (#DC143C) with glow effect
├─ "Game" = Gold (#FFD700)
└─ Clicking logo navigates to home
```

#### 2. Balance Box (Right Side - Center)
```
Displays: User's current balance in VNĐ
├─ Source: userData.balance from database
├─ Default: 1,000,000 VNĐ (mỗi tài khoản mới được 1 triệu)
├─ Format: "1.000.000 VNĐ" (using formatCurrency function)
├─ Update: Refreshes after each game/transaction
└─ Icon: Gold coin emoji/icon next to amount
```

#### 3. Avatar + Username (Right Side - End)
```
Avatar:
├─ Display: First letter of user's fullName
├─ Example: "Nguyễn Văn A" → Avatar shows "N"
├─ Style: Circular with red border
├─ Default: "U" if no name (fallback)

Username:
├─ Display: Full name from registration (fullName field)
├─ Example: "Nguyễn Văn A"
├─ Click to go to profile (future feature)
└─ Shown below avatar on mobile, beside on desktop
```

---

## 📊 Data Source & Flow

### When User Registers
```
Frontend (register.html)
    ↓ Submits form with fullName, email, phone, etc.
    ↓
Backend (POST /api/auth/register)
    ├─ Validates input
    ├─ Creates user record
    ├─ Sets: balance = 1,000,000 VNĐ
    ├─ Sets: rankPoints = 0
    └─ Returns: { user: { fullName, balance, ... } }
    ↓
Frontend stores in localStorage
```

### When User Logs In
```
Frontend (login.html)
    ↓ Submits username & password
    ↓
Backend (POST /api/auth/login)
    ├─ Validates credentials
    ├─ Generates JWT token
    └─ Returns:
       {
           "token": "JWT_...",
           "user": {
               "id": 1,
               "username": "user123",
               "fullName": "Nguyễn Văn A",  ← From registration
               "email": "email@example.com",
               "balance": 1000000,           ← Current balance
               "rankPoints": 0
           }
       }
    ↓
Frontend (auth.js - handleLogin function)
    ├─ Saves JWT token to localStorage
    ├─ Structures user data:
    │  {
    │      id: 1,
    │      fullName: "Nguyễn Văn A",
    │      balance: 1000000,
    │      rankPoints: 0,
    │      ...
    │  }
    ├─ Saves to localStorage
    └─ Redirects to /templates/home.html
    ↓
Frontend (home.html)
    ├─ Calls loadUserDataToHeader()
    └─ Header displays:
       ├─ Avatar: "N" (from fullName[0])
       ├─ Username: "Nguyễn Văn A" (fullName)
       └─ Balance: "1.000.000 VNĐ" (formatted)
```

---

## 🔧 Implementation Details

### loadUserDataToHeader() Function
Location: `js/auth.js`

```javascript
/**
 * Load user data into header
 * Called on every page with header
 * Reads from localStorage
 */
function loadUserDataToHeader() {
    const userData = getUserData();  // From localStorage
    
    if (!userData) return;
    
    // Update avatar - first letter of fullName
    const avatarEl = document.querySelector('.avatar');
    if (avatarEl && userData.fullName) {
        avatarEl.textContent = userData.fullName.charAt(0).toUpperCase();
    }
    
    // Update username - display fullName
    const usernameEl = document.querySelector('.username');
    if (usernameEl && userData.fullName) {
        usernameEl.textContent = userData.fullName;
    }
    
    // Update balance - display current balance
    const balanceEl = document.querySelector('.balance-amount');
    if (balanceEl) {
        const balance = userData.balance || 1000000;  // Default 1M
        balanceEl.textContent = formatCurrency(balance);
    }
}
```

### Login Flow (Updated)
Location: `js/auth.js - handleLogin()`

When login succeeds:
```javascript
// 1. Save JWT token
saveAuthToken(response.token);

// 2. Structure user data with fullName and balance
const userData = {
    id: response.user.id,
    username: response.user.username,
    fullName: response.user.fullName,    // From registration
    email: response.user.email,
    balance: response.user.balance || 1000000,  // Default 1M
    rankPoints: response.user.rankPoints || 0
};

// 3. Save to localStorage
saveUserData(userData);

// 4. Redirect to home
navigateTo('/templates/home.html');
```

### Header Template
Location: `templates/home.html`

```html
<header class="header">
    <!-- Logo -->
    <div class="logo">
        <span class="logo-tet">Tet</span><span class="logo-game">Game</span>
    </div>
    
    <div class="header-right">
        <!-- Balance Box -->
        <div class="balance-box">
            <span class="balance-amount">13.000.000 VNĐ</span>  <!-- Updated by JS -->
            <div class="coin-icon"></div>
        </div>
        
        <!-- User Info -->
        <div class="user-info" onclick="goToProfile()">
            <div class="avatar">A</div>  <!-- Updated by JS -->
            <span class="username">Người Chơi</span>  <!-- Updated by JS -->
        </div>
    </div>
</header>
```

---

## 🎨 Styling Reference

### CSS Classes
```css
.header {
    /* Header container */
    background: linear-gradient(90deg, #1a1a1a 0%, #2a1a1a 50%, #1a1a1a 100%);
    border-bottom: 3px solid #DC143C;
    padding: 20px 40px;
}

.logo {
    /* Logo container */
    font-size: 2.2rem;
    animation: glow 2.5s ease-in-out infinite;
}

.logo-tet {
    color: #DC143C;  /* Red */
}

.logo-game {
    color: #FFD700;  /* Gold */
    text-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
}

.balance-box {
    /* Balance display */
    background: linear-gradient(135deg, rgba(42, 42, 42, 0.9), rgba(26, 26, 26, 0.9));
    border: 2px solid #3a3a3a;
    border-radius: 10px;
    padding: 12px 20px;
}

.balance-amount {
    /* Balance text */
    font-weight: 700;
    font-size: 1.1rem;
    color: #FFD700;  /* Gold */
}

.coin-icon {
    /* Coin icon */
    width: 28px;
    height: 28px;
    background: linear-gradient(135deg, #FFD700, #FFA500);
    border-radius: 50%;
    box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.avatar {
    /* User avatar */
    width: 45px;
    height: 45px;
    border-radius: 50%;
    border: 3px solid #DC143C;  /* Red border */
    background: linear-gradient(135deg, #DC143C, #8B0000);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    color: white;
    font-size: 1.2rem;
}

.username {
    /* Username text */
    font-weight: 600;
    color: #f0f0f0;
    font-size: 1rem;
}
```

---

## 📱 Responsive Behavior

### Desktop (1024px+)
```
[TetGame] ────────── [Balance] [Avatar Name]
  Logo                Box        User Info
```

### Tablet (768-1023px)
```
[TetGame]
  Logo

[Balance] [Avatar Name]
  Box      User Info
```

### Mobile (480-767px)
```
[TetGame]
  Logo

[Balance Box]
[Avatar + Name]
```

### Small Mobile (<480px)
```
[TetGame]
  Logo

[Balance: 1M VNĐ]
[Avatar] Name
```

---

## 🔄 Data Update Scenarios

### After Login
```
✓ Avatar updated to first letter of fullName
✓ Username updated to fullName
✓ Balance updated to current amount
```

### After Game Completion
```
Backend updates balance in database
    ↓
Frontend fetches updated user profile (API call)
    ↓
Calls loadUserDataToHeader()
    ↓
Balance displays updated amount
```

### After Daily Login Bonus
```
Backend adds balance to user
    ↓
Frontend refreshes user data
    ↓
Header balance updates
```

### After Mission Completion
```
Backend adds balance to user for mission reward
    ↓
Frontend updates localStorage & header
    ↓
New balance displays in header
```

---

## 🧪 Testing the Header

### Manual Test Checklist
```
[ ] Login with test account
    └─ Verify avatar shows first letter of fullName
    └─ Verify username shows fullName (not account username)
    └─ Verify balance shows correct formatted number

[ ] Check different fullNames
    └─ "Nguyễn Văn A" → Avatar "N"
    └─ "John Doe" → Avatar "J"
    └─ "李明" → Avatar "李"

[ ] Check balance formatting
    └─ 1,000,000 → "1.000.000 VNĐ"
    └─ 500,000 → "500.000 VNĐ"
    └─ 10,000,000 → "10.000.000 VNĐ"

[ ] Test responsiveness
    └─ Desktop: Header is one line
    └─ Tablet: Header may wrap
    └─ Mobile: Stacked layout
    └─ Small mobile: Full width, readable

[ ] Click avatar/name
    └─ Should navigate to profile page (future)
```

---

## 🐛 Troubleshooting

### Avatar Shows "U" Instead of First Letter
**Issue**: fullName not provided in user data
**Solution**: 
- Check backend returns fullName in login response
- Verify localStorage contains fullName field
- Check browser console for errors

### Balance Shows "0" or "undefined"
**Issue**: balance not in user data
**Solution**:
- Backend should return balance in login response
- Default to 1,000,000 if balance is null
- Check database has balance field

### Username Shows "Người Chơi" (Default)
**Issue**: fullName is empty
**Solution**:
- Ensure registration saves fullName
- Check database User table has fullName
- Verify login response includes fullName

### Header Doesn't Update After Game
**Issue**: localStorage not updated
**Solution**:
- After game ends, fetch updated user profile
- Update localStorage with new balance
- Call loadUserDataToHeader() to refresh

---

## 📝 API Requirements Summary

### Register Endpoint Should Return
```json
{
    "user": {
        "id": 1,
        "fullName": "Họ và tên",
        "balance": 1000000,
        "rankPoints": 0
    }
}
```

### Login Endpoint Should Return
```json
{
    "token": "JWT_TOKEN",
    "user": {
        "id": 1,
        "username": "user123",
        "fullName": "Họ và tên",      ← REQUIRED for header
        "balance": 1000000,           ← REQUIRED for header
        "rankPoints": 0,
        "email": "email@example.com"
    }
}
```

---

## 💾 localStorage Structure

After successful login:
```javascript
localStorage.authToken = "JWT_TOKEN_HERE";

localStorage.userData = JSON.stringify({
    id: 1,
    username: "user123",
    fullName: "Nguyễn Văn A",      // Used in header
    email: "email@example.com",
    balance: 1000000,              // Used in header
    rankPoints: 0,
    avatarUrl: "profile.jpg"
});
```

When header loads:
1. Read userData from localStorage
2. Extract fullName → Display as username
3. Extract balance → Display formatted amount
4. Extract fullName first letter → Display in avatar

---

**Status**: ✅ Updated & Ready for Backend Implementation

The header component is fully functional and ready to display:
- User's fullName from registration
- User's current balance from database
- Properly formatted currency display

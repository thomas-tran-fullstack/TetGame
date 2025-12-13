# TetGame Frontend - Setup & Deployment Guide

## ✅ Frontend Migration - What Was Done

### 1. **Folder Structure Created**
```
backend/src/main/resources/static/
├── css/                    # Stylesheets
├── js/                     # JavaScript files
└── templates/              # HTML pages
```

### 2. **CSS Files Created**
- **global.css** (723 lines)
  - Color scheme: Red (#DC143C) + Black (#1a1a1a) + Gold accent
  - Responsive design (desktop, tablet, mobile)
  - Animations (fade-in, slide-in, glow, pulse)
  - Components: buttons, forms, alerts, containers
  - Media queries for 4 breakpoints

- **index.css** - Welcome page styling
- **auth.css** - Login/Register/Forgot-Password styling
- **register.css** - Password strength indicator
- **header.css** - Header component with logo, balance, avatar
- **home.css** - Game selector carousel and layout

### 3. **HTML Pages Created**

| Page | Path | Purpose |
|------|------|---------|
| Welcome | `/templates/index.html` | Landing page - Xin Chào + Login button |
| Login | `/templates/login.html` | Username/Password login |
| Register | `/templates/register.html` | User registration with validation |
| Forgot Password | `/templates/forgot-password.html` | 3-step password reset (email→OTP→new pass) |
| Home | `/templates/home.html` | Dashboard with header + game selector |

### 4. **JavaScript Files Created**

| File | Functions |
|------|-----------|
| **app.js** | Navigation, API helpers, storage, utilities |
| **validation.js** | Email, phone, username, password, age validation |
| **auth.js** | Login/logout, header loading, auth check |
| **register.js** | Registration form, password strength validation |
| **forgot-password.js** | 3-step password reset flow |
| **home.js** | Game carousel, keyboard/touch support |

### 5. **Features Implemented**

✅ **UI/UX Design**
- Red & Black color scheme with Gold accents
- Professional, elegant, responsive layout
- Fade-in animations on page load
- Glow effect on logo
- Hover effects on buttons and interactive elements

✅ **Authentication Pages**
- Login with username & password
- Register with full validation (age ≥16, email format, phone 10-11 digits)
- Password strength indicator with visual feedback
- Forgot password with OTP verification (3 steps)

✅ **Form Validation**
- Real-time validation feedback
- Specific error messages in Vietnamese
- Password match checking
- DOB validation (≥16 years old)
- Email, phone, username format validation

✅ **Responsive Design**
- Mobile-first approach
- Touch swipe support for game carousel
- Keyboard navigation (arrow keys, enter)
- Optimized layouts for:
  - Desktop (1024px+)
  - Tablet (768px-1023px)
  - Mobile (480px-767px)
  - Small Mobile (<480px)

✅ **Alerts & Feedback**
- Success alerts (green)
- Error alerts (red)
- Warning alerts (yellow)
- Auto-dismiss after 5 seconds
- Manual close button

✅ **Header Component**
- TetGame logo with glowing effect
- Balance display with currency formatting
- User avatar + name
- Responsive for all screen sizes

✅ **Game Carousel**
- Previous/Next buttons
- Keyboard navigation (← →)
- Touch swipe support
- Game name display
- Coming-soon message for inactive games

✅ **Local Storage**
- Auth token management
- User data persistence
- Storage with expiration support

✅ **API Integration Ready**
- Fetch API helpers (GET, POST, PUT)
- Bearer token authentication
- Error handling
- Response parsing

## 🔧 Backend Configuration Required

### 1. **Spring Boot Application Properties**
Add to `application.properties` or `application.yml`:

```properties
# Static Resources
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.period=31536000
spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**

# View Resolver for HTML pages
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html
```

### 2. **Controller for Frontend Routes**
Create a frontend controller to serve HTML pages:

```java
@Controller
public class FrontendController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/templates/index.html";
    }
    
    @GetMapping("/login")
    public String login() {
        return "forward:/templates/login.html";
    }
    
    @GetMapping("/register")
    public String register() {
        return "forward:/templates/register.html";
    }
    
    @GetMapping("/home")
    public String home() {
        return "forward:/templates/home.html";
    }
    
    // ... other routes
}
```

### 3. **Existing Backend Files to Update**
The following API endpoints need to be implemented in your backend:

```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/request-otp
POST /api/auth/verify-otp
POST /api/auth/reset-password
GET  /api/user/profile
```

## 🐳 Docker Updates Required

### Update `Dockerfile.backend`:

```dockerfile
FROM maven:3.8.1-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/backend-*.jar app.jar
EXPOSE 8080

# Ensure static files are served
ENV SPRING_WEB_RESOURCES_STATIC_LOCATIONS=classpath:/static/

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Update `docker-compose.yml`:

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/tetgame
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=password
    depends_on:
      - db
    volumes:
      - ./backend/src/main/resources/static:/app/BOOT-INF/classes/static

  db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=tetgame
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 🚀 Deployment Steps

### Local Development
1. **Build Backend:**
   ```bash
   cd backend
   mvn clean package
   ```

2. **Run with Docker:**
   ```bash
   docker-compose up --build
   ```

3. **Access Application:**
   - Open browser: `http://localhost:8080`
   - Should redirect to `http://localhost:8080/templates/index.html`

### Production Deployment (Render.com)

1. **Update `render.yaml`:**
   ```yaml
   services:
     - type: web
       name: tetgame-backend
       env: java
       plan: free
       region: singapore
       
       # Build command
       buildCommand: "cd backend && mvn clean package -DskipTests"
       
       # Start command
       startCommand: "java -jar target/backend-*.jar"
       
       # Environment variables
       envVars:
         - key: SPRING_JPA_HIBERNATE_DDL_AUTO
           value: update
         - key: SPRING_DATASOURCE_URL
           value: ${DATABASE_URL}
       
       # Static files
       staticSite:
         sourceDir: backend/src/main/resources/static
   ```

2. **Deploy to Render:**
   ```bash
   git push  # Push to GitHub
   # Render will auto-deploy from connected GitHub repo
   ```

3. **Database Setup:**
   - Create MySQL database on Render
   - Update DATABASE_URL environment variable

## 📋 Testing Checklist

### Frontend Pages
- [ ] Index page loads with fade-in animation
- [ ] Login page functional with validation
- [ ] Register page with all field validations
- [ ] Forgot password 3-step flow
- [ ] Home page with game carousel
- [ ] Header with balance and user info
- [ ] Back buttons work on all pages
- [ ] Alerts display correctly (success/error/warning)

### Form Validation
- [ ] Email validation (requires @ and .)
- [ ] Phone validation (10-11 digits)
- [ ] Username validation (no spaces, no special chars)
- [ ] Password strength indicator works
- [ ] Password match checking
- [ ] Age validation (≥16)
- [ ] Full name validation

### Responsive Design
- [ ] Desktop layout (1024px+) looks good
- [ ] Tablet layout (768px-1023px) responsive
- [ ] Mobile layout (480px-767px) stacked
- [ ] Small mobile (<480px) readable
- [ ] Touch targets are 44px+ on mobile
- [ ] Images scale properly

### Game Carousel (Home)
- [ ] Previous/Next buttons work
- [ ] Arrow keys work (← →)
- [ ] Touch swipe works (mobile)
- [ ] Game names display correctly
- [ ] Coming-soon alert shows for inactive games

### Browser Compatibility
- [ ] Chrome/Edge (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Mobile browsers (iOS Safari, Chrome Mobile)

## 🔒 Security Considerations

1. **HTTPS:** Use HTTPS in production
2. **CORS:** Configure CORS properly for API endpoints
3. **XSS Protection:** All user inputs are escaped
4. **CSRF Protection:** Implement CSRF tokens for POST requests
5. **Auth Headers:** Token sent in Authorization header
6. **Password Security:** Passwords are only transmitted over HTTPS

## 📚 Additional Resources

- See `FRONTEND_README.md` for detailed API endpoints
- CSS variable guide in `global.css`
- JavaScript function documentation in each JS file
- HTML page structure in each template

## 🎯 Next Steps

1. **Implement Backend APIs:**
   - Auth endpoints (login, register, OTP, reset password)
   - User profile endpoints
   - Game endpoints

2. **Create Game Pages:**
   - Tiến Lên game board
   - Game room system
   - Game lobby

3. **Add Features:**
   - User profile page
   - Ranking page
   - Mission/task system
   - Spin wheel
   - Chat & voice functionality

4. **Database:**
   - Create user, game, transaction tables
   - Setup authentication with JWT tokens

---

**Frontend Version**: 1.0.0  
**Last Updated**: December 2025  
**Status**: ✅ Complete & Ready for Backend Integration

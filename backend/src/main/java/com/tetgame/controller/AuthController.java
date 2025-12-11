package com.tetgame.controller;

import com.tetgame.config.JwtProvider;
import com.tetgame.controller.dto.LoginRequest;
import com.tetgame.controller.dto.LoginResponse;
import com.tetgame.controller.dto.RegisterRequest;
import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.service.UserService;
import com.tetgame.modules.user.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtProvider jwtProvider, UserService userService, WalletService walletService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        String token = jwtProvider.generateToken(req.getUsername());
        long expiresIn = System.currentTimeMillis() + 24L * 3600L * 1000L;
        var userOpt = userService.findByUsername(req.getUsername());
        String avatar = userOpt.map(User::getAvatarUrl).orElse(null);
        long balance = userOpt.map(u -> walletService.getBalance(u.getId())).orElse(0L);
        return ResponseEntity.ok(new LoginResponse(token, expiresIn, req.getUsername(), avatar, balance));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User u = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .phone(req.getPhone())
                .fullName(req.getFullName())
                .dob(req.getDob())
                .build();
        User saved = userService.createUser(u);
        String token = jwtProvider.generateToken(saved.getUsername());
        long expiresIn = System.currentTimeMillis() + 24L * 3600L * 1000L;
        long balance = walletService.getBalance(saved.getId());
        return ResponseEntity.ok(new LoginResponse(token, expiresIn, saved.getUsername(), saved.getAvatarUrl(), balance));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("{\"message\": \"Logged out successfully\"}");
    }
}

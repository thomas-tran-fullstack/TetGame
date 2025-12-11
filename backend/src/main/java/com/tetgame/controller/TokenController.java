package com.tetgame.controller;

import com.tetgame.config.JwtProvider;
import com.tetgame.controller.dto.LoginResponse;
import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.repository.UserRepository;
import com.tetgame.modules.user.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class TokenController {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final WalletService walletService;

    public TokenController(JwtProvider jwtProvider, UserRepository userRepository, WalletService walletService) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        
        String username = auth.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) throw new BadCredentialsException("User not found");

        User user = userOpt.get();
        String newToken = jwtProvider.generateToken(username);
        long balance = walletService.getBalance(user.getId());
        long expiresIn = System.currentTimeMillis() + 24L * 3600L * 1000L;

        return ResponseEntity.ok(new LoginResponse(newToken, expiresIn, username, user.getAvatarUrl(), balance));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }

        String token = header.substring(7);
        boolean valid = jwtProvider.validateToken(token);
        if (valid) {
            String username = jwtProvider.getSubject(token);
            return ResponseEntity.ok(Map.of("valid", true, "username", username));
        }
        return ResponseEntity.status(401).body(Map.of("valid", false));
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<?> oauthCallback(@RequestParam String token, @RequestParam String username, 
                                           @RequestParam(required = false) String avatar, @RequestParam(required = false, defaultValue = "0") long balance) {
        // This endpoint can return the token info as JSON for frontend to store
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", username,
                "avatarUrl", avatar != null ? avatar : "",
                "balance", balance
        ));
    }
}

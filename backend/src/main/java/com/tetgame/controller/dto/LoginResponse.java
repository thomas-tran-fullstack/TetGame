package com.tetgame.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private final String token;
    private final long expiresIn;
    private final String username;
    private final String avatarUrl;
    private final long balance;
}

package com.tetgame.modules.user.service;

import java.util.UUID;

public interface WalletService {
    long getBalance(UUID userId);
    void add(UUID userId, long amount);
    void subtract(UUID userId, long amount) throws IllegalArgumentException;
}

package com.tetgame.modules.user.service.impl;

import com.tetgame.modules.user.entity.Wallet;
import com.tetgame.modules.user.repository.WalletRepository;
import com.tetgame.modules.user.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public long getBalance(UUID userId) {
        return walletRepository.findByUserId(userId).map(Wallet::getBalance).orElse(0L);
    }

    @Override
    @Transactional
    public void add(UUID userId, long amount) {
        Wallet w = walletRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        w.setBalance(w.getBalance() + amount);
        w.setUpdatedAt(OffsetDateTime.now());
        walletRepository.save(w);
    }

    @Override
    @Transactional
    public void subtract(UUID userId, long amount) throws IllegalArgumentException {
        Wallet w = walletRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        if (w.getBalance() - amount < 0) throw new IllegalArgumentException("Insufficient balance");
        w.setBalance(w.getBalance() - amount);
        w.setUpdatedAt(OffsetDateTime.now());
        walletRepository.save(w);
    }
}

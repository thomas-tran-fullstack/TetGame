package com.tetgame.modules.user.service.impl;

import com.tetgame.modules.user.entity.Rank;
import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.entity.Wallet;
import com.tetgame.modules.user.repository.RankRepository;
import com.tetgame.modules.user.repository.UserRepository;
import com.tetgame.modules.user.repository.WalletRepository;
import com.tetgame.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RankRepository rankRepository;

    public UserServiceImpl(UserRepository userRepository, WalletRepository walletRepository, RankRepository rankRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.rankRepository = rankRepository;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        User saved = userRepository.save(user);

        // create wallet
        Wallet w = Wallet.builder().user(saved).balance(0L).build();
        walletRepository.save(w);

        // create rank
        Rank r = Rank.builder().user(saved).points(0).build();
        rankRepository.save(r);

        return saved;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}

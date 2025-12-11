package com.tetgame.modules.user.service;

import com.tetgame.modules.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User updateUser(User user);
    void deleteUser(UUID id);
}

package com.tetgame.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisTestController {

    private final StringRedisTemplate redis;

    @GetMapping("/test/redis")
    public String test() {
        try {
            redis.opsForValue().set("test_key", "hello_redis");
            String value = redis.opsForValue().get("test_key");
            return "Redis OK: " + value;
        } catch (Exception e) {
            return "Redis ERROR: " + e.getMessage();
        }
    }
}

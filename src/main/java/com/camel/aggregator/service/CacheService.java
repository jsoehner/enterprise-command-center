package com.camel.aggregator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.NonNull;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void put(@NonNull String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("[!] Redis unreachable. Skipping cache write.");
        }
    }

    public Object get(@NonNull String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            System.err.println("[!] Redis unreachable. Skipping cache read.");
            return null;
        }
    }
}

package com.camel.aggregator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void put(@NonNull String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("[!] Redis unreachable. Skipping cache write.", e);
        }
    }

    public Object get(@NonNull String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("[!] Redis unreachable. Skipping cache read.", e);
            return null;
        }
    }
}

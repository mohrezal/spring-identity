package com.github.mohrezal.identity.shared.redis;

import com.github.mohrezal.identity.shared.enums.RedisKey;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void set(RedisKey key, String value, Duration ttl, String... keyValues) {

        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Redis TTL must be positive");
        }

        redisTemplate.opsForValue().set(key.resolve(keyValues), value, ttl);
    }

    public Optional<String> get(RedisKey key, String... keyValues) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key.resolve(keyValues)))
                .filter(StringUtils::hasText);
    }

    public boolean delete(RedisKey key, String... keyValues) {
        return Boolean.TRUE.equals(redisTemplate.delete(key.resolve(keyValues)));
    }
}

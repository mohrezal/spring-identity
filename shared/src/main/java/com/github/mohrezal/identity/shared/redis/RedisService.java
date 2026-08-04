package com.github.mohrezal.identity.shared.redis;

import com.github.mohrezal.identity.shared.enums.RedisKey;
import io.lettuce.core.RedisException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    private static final DefaultRedisScript<String> INCREMENT_WITH_TTL_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local count = redis.call('INCR', KEYS[1])
                    local ttl = redis.call('PTTL', KEYS[1])
                    if ttl < 0 then
                        redis.call('PEXPIRE', KEYS[1], ARGV[1])
                        ttl = ARGV[1]
                    end
                    return count .. ':' .. math.ceil(ttl / 1000)
                    """,
                    String.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    public void set(RedisKey redisKey, Object value, String... keyValues) {
        set(redisKey, value, redisKey.getTtl(), keyValues);
    }

    public void set(RedisKey redisKey, Object value, Duration ttl, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            redisTemplate.opsForValue().set(resolvedKey, value, ttl);
        } catch (RedisException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public Optional<Duration> getTimeToLive(RedisKey redisKey, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            var seconds = redisTemplate.getExpire(resolvedKey, TimeUnit.SECONDS);
            if (seconds == null || seconds < 0) {
                return Optional.empty();
            }
            return Optional.of(Duration.ofSeconds(seconds));
        } catch (RedisException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public <T> Optional<T> get(RedisKey redisKey, Class<T> type, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            var value = redisTemplate.opsForValue().get(resolvedKey);

            if (value == null) {
                return Optional.empty();
            }

            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }

            return Optional.of(redisObjectMapper.convertValue(value, type));
        } catch (RedisException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public Boolean delete(RedisKey redisKey, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            return redisTemplate.delete(resolvedKey);
        } catch (RedisException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public <T> Optional<T> consume(RedisKey redisKey, Class<T> type, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            var value = redisTemplate.opsForValue().getAndDelete(resolvedKey);

            if (value == null) {
                return Optional.empty();
            }

            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }

            return Optional.of(redisObjectMapper.convertValue(value, type));
        } catch (RedisException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public CounterState increment(RedisKey redisKey, Duration ttl, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            var result =
                    redisTemplate.execute(
                            INCREMENT_WITH_TTL_SCRIPT,
                            RedisSerializer.string(),
                            RedisSerializer.string(),
                            java.util.List.of(resolvedKey),
                            String.valueOf(ttl.toMillis()));

            if (!result.contains(":")) {
                throw new RuntimeException("Redis increment result is invalid");
            }

            var parts = result.split(":");
            return new CounterState(toInt(parts[0]), toInt(parts[1]));
        } catch (RedisException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.parseInt(value.toString());
    }
}

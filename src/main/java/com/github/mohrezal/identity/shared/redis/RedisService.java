package com.github.mohrezal.identity.shared.redis;

import com.github.mohrezal.identity.shared.enums.RedisKey;
import io.lettuce.core.RedisException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    public void set(RedisKey redisKey, Object value, String... keyValues) {
        var resolvedKey = redisKey.resolve(keyValues);

        try {
            redisTemplate.opsForValue().set(resolvedKey, value, redisKey.getTtl());
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
        var value = get(redisKey, type, keyValues);
        value.ifPresent(ignored -> delete(redisKey, keyValues));
        return value;
    }
}

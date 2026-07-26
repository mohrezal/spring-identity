package com.github.mohrezal.identity.management.endpoint;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "audit-redis")
@RequiredArgsConstructor
public class AuditRedisEndpoint {

    private final RedisConnectionFactory redisConnectionFactory;

    @ReadOperation
    public Map<String, Object> health() {
        try (var connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            return Map.of("status", "UP", "redis", "connected");
        } catch (Exception e) {
            return Map.of("status", "DOWN", "error", e.getMessage());
        }
    }
}

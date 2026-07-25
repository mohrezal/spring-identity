package com.github.mohrezal.identity.management.endpoint;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "audit-rabbitmq")
@RequiredArgsConstructor
public class AuditRabbitMQEndpoint {

    private final ConnectionFactory connectionFactory;

    @ReadOperation
    public Map<String, Object> health() {
        try (var connection = connectionFactory.createConnection()) {
            return Map.of("status", "UP", "rabbitmq", "connected");
        } catch (Exception e) {
            return Map.of("status", "DOWN", "error", e.getMessage());
        }
    }
}

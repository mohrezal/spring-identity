package com.github.mohrezal.identity.audit.consumer;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.validation.AuditSchemaValidator;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditMessageConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditSchemaValidator schemaValidator;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConstants.Audit.Queue.AUDIT)
    public void consume(AuditEvent event) {
        var jsonPayload = objectMapper.writeValueAsString(event);
        var violations = schemaValidator.validate(jsonPayload);

        if (!violations.isEmpty()) {
            log.warn(
                    "Audit event failed schema validation eventId={}, violations={}",
                    event.eventId(),
                    violations);
            throw new AmqpRejectAndDontRequeueException("Schema validation failed");
        }

        redisTemplate.opsForList().rightPush(RedisKey.AUDIT_EVENTS.resolve(), event);

        log.info(
                "Stored audit event eventId={}, eventType={}, traceId={}",
                event.eventId(),
                event.eventType(),
                event.traceId());
    }
}

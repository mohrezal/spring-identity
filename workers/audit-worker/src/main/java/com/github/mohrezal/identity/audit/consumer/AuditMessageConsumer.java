package com.github.mohrezal.identity.audit.consumer;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditMessageConsumer {

    private static final String AUDIT_EVENTS_KEY = "audit:events";

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConstants.Audit.Queue.AUDIT)
    public void consume(AuditEvent event) {
        redisTemplate.opsForList().rightPush(AUDIT_EVENTS_KEY, event);

        log.info(
                "Stored audit event eventId={}, eventType={}, traceId={}",
                event.eventId(),
                event.eventType(),
                event.traceId());
    }
}

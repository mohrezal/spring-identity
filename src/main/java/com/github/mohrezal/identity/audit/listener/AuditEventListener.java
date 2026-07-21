package com.github.mohrezal.identity.audit.listener;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import com.github.mohrezal.identity.shared.rabbitmq.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {
    private final RabbitMQPublisher rabbitMQPublisher;

    @EventListener
    public void handle(AuditEvent event) {
        rabbitMQPublisher.publish(
                RabbitMQConstants.Audit.EXCHANGE, RabbitMQConstants.Audit.RoutingKey.AUDIT, event);

        log.info(
                "Published audit event eventId={}, eventType={}, traceId={}",
                event.eventId(),
                event.eventType(),
                event.traceId());
    }
}

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

    // Uses @EventListener (not @TransactionalEventListener) because audit events
    // are published outside the command's transaction boundary — the controller
    // publishes after LoginCommand's transaction has already committed, and the
    // exception handler publishes after rollback. There is no active transaction
    // to hook into, so transactional semantics don't apply.
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

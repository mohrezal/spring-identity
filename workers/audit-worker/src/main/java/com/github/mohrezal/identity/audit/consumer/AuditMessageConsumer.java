package com.github.mohrezal.identity.audit.consumer;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.model.AuditEventEntity;
import com.github.mohrezal.identity.audit.repository.AuditEventRepository;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditMessageConsumer {

    private final AuditEventRepository auditEventRepository;

    @RabbitListener(queues = RabbitMQConstants.Audit.Queue.AUDIT)
    public void consume(AuditEvent event) {
        var entity =
                AuditEventEntity.builder()
                        .schemaVersion(event.schemaVersion())
                        .eventId(event.eventId())
                        .eventType(event.eventType())
                        .outcome(event.outcome())
                        .occurredAt(event.occurredAt())
                        .traceId(event.traceId())
                        .actorUserId(event.actor() != null ? event.actor().userId() : null)
                        .subjectUserId(event.subject() != null ? event.subject().userId() : null)
                        .subjectEmail(event.subject() != null ? event.subject().email() : null)
                        .sessionId(event.session() != null ? event.session().sessionId() : null)
                        .requestId(event.request() != null ? event.request().requestId() : null)
                        .clientRequestId(
                                event.request() != null ? event.request().clientRequestId() : null)
                        .ipAddress(event.request() != null ? event.request().ipAddress() : null)
                        .userAgent(event.request() != null ? event.request().userAgent() : null)
                        .forwardedHost(
                                event.request() != null ? event.request().forwardedHost() : null)
                        .forwardedProto(
                                event.request() != null ? event.request().forwardedProto() : null)
                        .reason(event.reason())
                        .build();

        auditEventRepository.save(entity);

        log.info(
                "Persisted audit event eventId={}, eventType={}, traceId={}",
                event.eventId(),
                event.eventType(),
                event.traceId());
    }
}

package com.github.mohrezal.identity.audit.service;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.entity.AuditEventEntity;
import com.github.mohrezal.identity.audit.repository.AuditEventRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public Optional<AuditEventEntity> createEvent(AuditEvent event) {
        if (auditEventRepository.existsByEventId(event.eventId())) {
            return Optional.empty();
        }

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

        var saved = auditEventRepository.save(entity);
        return Optional.of(saved);
    }
}

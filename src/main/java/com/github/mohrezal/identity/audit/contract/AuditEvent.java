package com.github.mohrezal.identity.audit.contract;

import com.github.mohrezal.identity.audit.enums.AuditEventType;
import com.github.mohrezal.identity.audit.enums.AuditOutcome;
import com.github.mohrezal.identity.shared.enums.AppMessage;
import java.time.Instant;

public record AuditEvent(
        int schemaVersion,
        String eventId,
        AuditEventType eventType,
        AuditOutcome outcome,
        Instant occurredAt,
        String traceId,
        AuditActor actor,
        AuditSubject subject,
        AuditSession session,
        AuditRequest request,
        AppMessage reason) {}

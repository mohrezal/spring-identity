package com.github.mohrezal.identity.audit.model;

import com.github.mohrezal.identity.audit.enums.AuditEventType;
import com.github.mohrezal.identity.audit.enums.AuditOutcome;
import com.github.mohrezal.identity.shared.enums.AppMessage;
import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
        int schemaVersion,
        UUID eventId,
        AuditEventType eventType,
        AuditOutcome outcome,
        Instant occurredAt,
        String traceId,
        AuditActor actor,
        AuditSubject subject,
        AuditSession session,
        AuditRequest request,
        AppMessage reason) {}

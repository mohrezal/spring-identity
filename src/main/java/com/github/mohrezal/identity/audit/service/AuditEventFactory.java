package com.github.mohrezal.identity.audit.service;

import com.github.mohrezal.identity.audit.contract.AuditActor;
import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.contract.AuditRequest;
import com.github.mohrezal.identity.audit.contract.AuditSession;
import com.github.mohrezal.identity.audit.contract.AuditSubject;
import com.github.mohrezal.identity.audit.enums.AuditEventType;
import com.github.mohrezal.identity.audit.enums.AuditOutcome;
import com.github.mohrezal.identity.shared.enums.AppMessage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuditEventFactory {

    private static final int SCHEMA_VERSION = 1;

    public AuditEvent loginSucceeded(
            AuditRequestContext requestContext, UUID userId, String email, String sessionId) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.LOGIN_SUCCEEDED,
                AuditOutcome.SUCCESS,
                currentTimestamp(),
                traceId,
                new AuditActor(userId),
                new AuditSubject(userId, normalizeEmail(email)),
                new AuditSession(sessionId),
                createAuditRequest(requestContext, traceId),
                null);
    }

    public AuditEvent loginFailed(
            AuditRequestContext requestContext, String attemptedEmail, AppMessage reason) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(reason, "reason must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.LOGIN_FAILED,
                AuditOutcome.FAILURE,
                currentTimestamp(),
                traceId,
                new AuditActor(null),
                new AuditSubject(null, normalizeEmail(attemptedEmail)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                reason);
    }

    private AuditRequest createAuditRequest(AuditRequestContext requestContext, String traceId) {
        return new AuditRequest(
                traceId,
                requestContext.clientRequestId(),
                requestContext.ipAddress(),
                requestContext.userAgent(),
                requestContext.forwardedHost(),
                requestContext.forwardedProto());
    }

    private String requireTraceId(AuditRequestContext requestContext) {
        return Objects.requireNonNull(requestContext.traceId(), "traceId must not be null");
    }

    private String normalizeEmail(String email) {
        return Objects.requireNonNull(email, "email must not be null")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private Instant currentTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}

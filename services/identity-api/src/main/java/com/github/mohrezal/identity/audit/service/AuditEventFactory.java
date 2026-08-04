package com.github.mohrezal.identity.audit.service;

import com.github.mohrezal.identity.audit.contract.AuditActor;
import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.contract.AuditRequest;
import com.github.mohrezal.identity.audit.contract.AuditSession;
import com.github.mohrezal.identity.audit.contract.AuditSubject;
import com.github.mohrezal.identity.audit.enums.AuditEventType;
import com.github.mohrezal.identity.audit.enums.AuditOutcome;
import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.service.EmailAddressNormalizer;
import com.github.mohrezal.identity.shared.service.HttpRequestContextService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventFactory {

    private static final int SCHEMA_VERSION = 1;

    private final HttpRequestContextService httpRequestContextService;

    public AuditRequestContext createAuditRequestContext(HttpServletRequest request) {
        return new AuditRequestContext(
                httpRequestContextService.requireTraceId(),
                httpRequestContextService.getClientRequestId(request).orElse(null),
                httpRequestContextService.getClientIp(request),
                httpRequestContextService.getUserAgent(request).orElse(null),
                httpRequestContextService.getForwardedHost(request).orElse(null),
                httpRequestContextService.getForwardedProto(request).orElse(null));
    }

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
            AuditRequestContext requestContext, String attemptedEmail, ExceptionCode reason) {
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
                reason.name());
    }

    public AuditEvent registerStarted(AuditRequestContext requestContext, String email) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(email, "email must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.REGISTER_STARTED,
                AuditOutcome.SUCCESS,
                currentTimestamp(),
                traceId,
                new AuditActor(null),
                new AuditSubject(null, normalizeEmail(email)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                null);
    }

    public AuditEvent registerSucceeded(
            AuditRequestContext requestContext, UUID userId, String email) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.REGISTER_SUCCEEDED,
                AuditOutcome.SUCCESS,
                currentTimestamp(),
                traceId,
                new AuditActor(userId),
                new AuditSubject(userId, normalizeEmail(email)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                null);
    }

    public AuditEvent registerFailed(
            AuditRequestContext requestContext, String attemptedEmail, ExceptionCode reason) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(reason, "reason must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.REGISTER_FAILED,
                AuditOutcome.FAILURE,
                currentTimestamp(),
                traceId,
                new AuditActor(null),
                new AuditSubject(null, normalizeEmail(attemptedEmail)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                reason.name());
    }

    public AuditEvent emailVerificationSent(AuditRequestContext requestContext, String email) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(email, "email must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.EMAIL_VERIFICATION_SENT,
                AuditOutcome.SUCCESS,
                currentTimestamp(),
                traceId,
                new AuditActor(null),
                new AuditSubject(null, normalizeEmail(email)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                null);
    }

    public AuditEvent emailVerified(AuditRequestContext requestContext, UUID userId, String email) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.EMAIL_VERIFIED,
                AuditOutcome.SUCCESS,
                currentTimestamp(),
                traceId,
                new AuditActor(null),
                new AuditSubject(userId, normalizeEmail(email)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                null);
    }

    public AuditEvent emailVerificationFailed(
            AuditRequestContext requestContext, String email, ExceptionCode reason) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(reason, "reason must not be null");

        var traceId = requireTraceId(requestContext);

        return new AuditEvent(
                SCHEMA_VERSION,
                UUID.randomUUID().toString(),
                AuditEventType.EMAIL_VERIFICATION_FAILED,
                AuditOutcome.FAILURE,
                currentTimestamp(),
                traceId,
                new AuditActor(null),
                new AuditSubject(null, normalizeEmail(email)),
                new AuditSession(null),
                createAuditRequest(requestContext, traceId),
                reason.name());
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
        return Objects.requireNonNull(
                EmailAddressNormalizer.normalize(email), "email must not be null");
    }

    private Instant currentTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}

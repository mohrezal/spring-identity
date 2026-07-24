package com.github.mohrezal.identity.audit.model;

import com.github.mohrezal.identity.audit.enums.AuditEventType;
import com.github.mohrezal.identity.audit.enums.AuditOutcome;
import com.github.mohrezal.identity.shared.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_events")
@Getter
@NoArgsConstructor
@SuperBuilder
public class AuditEventEntity extends BaseModel {

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private AuditOutcome outcome;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "subject_user_id")
    private UUID subjectUserId;

    @Column(name = "subject_email")
    private String subjectEmail;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "client_request_id")
    private String clientRequestId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "forwarded_host")
    private String forwardedHost;

    @Column(name = "forwarded_proto")
    private String forwardedProto;

    @Column(name = "reason")
    private String reason;
}
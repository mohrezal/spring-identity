package com.github.mohrezal.identity.audit.model;

import java.util.UUID;

public record AuditRequest(
        UUID requestId,
        UUID clientRequestId,
        String ipAddress,
        String userAgent,
        String forwardedHost,
        String forwardedProto) {}

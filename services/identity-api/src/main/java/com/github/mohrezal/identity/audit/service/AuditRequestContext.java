package com.github.mohrezal.identity.audit.service;

public record AuditRequestContext(
        String traceId,
        String clientRequestId,
        String ipAddress,
        String userAgent,
        String forwardedHost,
        String forwardedProto) {}

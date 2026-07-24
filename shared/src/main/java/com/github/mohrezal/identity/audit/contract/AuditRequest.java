package com.github.mohrezal.identity.audit.contract;

public record AuditRequest(
        String requestId,
        String clientRequestId,
        String ipAddress,
        String userAgent,
        String forwardedHost,
        String forwardedProto) {}

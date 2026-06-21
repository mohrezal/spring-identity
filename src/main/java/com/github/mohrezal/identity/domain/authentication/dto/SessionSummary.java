package com.github.mohrezal.identity.domain.authentication.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionSummary(
        UUID id,
        String deviceInfo,
        String ipAddress,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        boolean isCurrentSession) {}

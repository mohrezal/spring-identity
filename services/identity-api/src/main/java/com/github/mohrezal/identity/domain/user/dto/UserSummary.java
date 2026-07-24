package com.github.mohrezal.identity.domain.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSummary(
        UUID id,
        String email,
        String firstName,
        String lastName,
        OffsetDateTime emailVerifiedAt,
        Boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}

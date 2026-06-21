package com.github.mohrezal.identity.domain.authentication.dto;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OAuthConnectionSummary(
        UUID id,
        OAuthProviderType provider,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}

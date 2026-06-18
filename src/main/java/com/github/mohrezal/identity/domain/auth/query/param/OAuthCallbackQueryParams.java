package com.github.mohrezal.identity.domain.auth.query.param;

import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;

public record OAuthCallbackQueryParams(
        OAuthProviderType provider,
        String code,
        OAuthStatePayload payload,
        String correlationId,
        String ipAddress,
        String userAgent) {}

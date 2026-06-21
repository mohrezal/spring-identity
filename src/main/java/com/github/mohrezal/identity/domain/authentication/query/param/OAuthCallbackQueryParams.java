package com.github.mohrezal.identity.domain.authentication.query.param;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;

public record OAuthCallbackQueryParams(
        OAuthProviderType provider,
        String code,
        String state,
        String correlationId,
        String ipAddress,
        String userAgent) {}

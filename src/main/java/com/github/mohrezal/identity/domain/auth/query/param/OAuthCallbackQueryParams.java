package com.github.mohrezal.identity.domain.auth.query.param;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;

public record OAuthCallbackQueryParams(
        OAuthProviderType provider,
        String code,
        String state,
        String ipAddress,
        String userAgent) {}

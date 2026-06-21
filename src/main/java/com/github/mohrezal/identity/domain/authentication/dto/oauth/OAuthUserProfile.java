package com.github.mohrezal.identity.domain.authentication.dto.oauth;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;

public record OAuthUserProfile(
        String providerUserId,
        String email,
        boolean emailVerified,
        String firstName,
        String lastName,
        OAuthProviderType provider) {}

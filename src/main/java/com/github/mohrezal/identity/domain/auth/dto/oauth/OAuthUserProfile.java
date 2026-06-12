package com.github.mohrezal.identity.domain.auth.dto.oauth;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;

public record OAuthUserProfile(
        String providerUserId,
        String email,
        String firstName,
        String lastName,
        OAuthProviderType provider) {}

package com.github.mohrezal.identity.domain.auth.dto.oauth;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.shared.service.EmailAddressNormalizer;

public record OAuthUserProfile(
        String providerUserId,
        String email,
        boolean emailVerified,
        String firstName,
        String lastName,
        OAuthProviderType provider) {

    public OAuthUserProfile {
        email = EmailAddressNormalizer.normalize(email);
    }
}

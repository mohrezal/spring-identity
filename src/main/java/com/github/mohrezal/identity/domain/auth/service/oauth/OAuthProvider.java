package com.github.mohrezal.identity.domain.auth.service.oauth;

import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;

public interface OAuthProvider {
    OAuthProviderType provider();

    String buildAuthorizationUrl(String state);

    OAuthUserProfile profile(String code);
}

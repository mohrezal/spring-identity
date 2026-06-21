package com.github.mohrezal.identity.domain.authentication.service.oauth;

import com.github.mohrezal.identity.domain.authentication.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;

public interface OAuthProvider {
    OAuthProviderType provider();

    String buildAuthorizationUrl(String state);

    OAuthUserProfile profile(String code);
}

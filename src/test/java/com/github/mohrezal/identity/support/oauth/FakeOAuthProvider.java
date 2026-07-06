package com.github.mohrezal.identity.support.oauth;

import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProvider;
import org.springframework.web.util.UriComponentsBuilder;

public final class FakeOAuthProvider implements OAuthProvider {

    public static final String AUTHORIZATION_URL = "https://oauth.test/authorize";

    @Override
    public OAuthProviderType provider() {
        return OAuthProviderType.GOOGLE;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZATION_URL)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public OAuthUserProfile profile(String code) {
        throw new UnsupportedOperationException("OAuth callback is not configured for this test");
    }
}

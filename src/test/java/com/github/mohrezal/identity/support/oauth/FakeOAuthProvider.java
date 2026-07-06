package com.github.mohrezal.identity.support.oauth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;

import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProvider;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import org.springframework.web.util.UriComponentsBuilder;

public final class FakeOAuthProvider implements OAuthProvider {

    public static final String AUTHORIZATION_URL = "https://oauth.test/authorize";
    public static final String CODE = "valid-authorization-code";
    public static final String PROVIDER_USER_ID = "google-user";

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
        if (!CODE.equals(code)) {
            throw new UnauthorizedException();
        }

        return new OAuthUserProfile(
                PROVIDER_USER_ID, EMAIL, true, "Test", "User", OAuthProviderType.GOOGLE);
    }
}

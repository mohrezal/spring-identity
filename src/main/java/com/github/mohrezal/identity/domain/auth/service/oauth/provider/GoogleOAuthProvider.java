package com.github.mohrezal.identity.domain.auth.service.oauth.provider;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.service.oauth.AbstractAuthorizationCodeOAuthProvider;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuthProvider extends AbstractAuthorizationCodeOAuthProvider {

    private final ApplicationProperties applicationProperties;

    @Override
    public OAuthProviderType provider() {
        return OAuthProviderType.GOOGLE;
    }

    @Override
    protected OAuthUserProfile profile(
            OAuth2AccessTokenResponse tokenResponse, ClientRegistration registration) {
        var jwt = decodeIdToken(requireIdToken(tokenResponse), registration);

        if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            throw new UnauthorizedException();
        }

        return new OAuthUserProfile(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                provider());
    }

    @Override
    protected ClientRegistration clientRegistration() {
        var google = applicationProperties.security().oAuth().google();
        return CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(google.clientId())
                .clientSecret(google.clientSecret())
                .redirectUri(google.redirectUri())
                .scope(google.scopes())
                .build();
    }
}

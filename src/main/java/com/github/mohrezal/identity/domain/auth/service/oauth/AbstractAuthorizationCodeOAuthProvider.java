package com.github.mohrezal.identity.domain.auth.service.oauth;

import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import java.util.List;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public abstract class AbstractAuthorizationCodeOAuthProvider implements OAuthProvider {

    private final RestClientAuthorizationCodeTokenResponseClient tokenClient =
            new RestClientAuthorizationCodeTokenResponseClient();

    @Override
    public String buildAuthorizationUrl(String state) {
        var registration = clientRegistration();
        var details = registration.getProviderDetails();

        return OAuth2AuthorizationRequest.authorizationCode()
                .clientId(registration.getClientId())
                .authorizationUri(details.getAuthorizationUri())
                .redirectUri(registration.getRedirectUri())
                .scopes(registration.getScopes())
                .state(state)
                .build()
                .getAuthorizationRequestUri();
    }

    @Override
    public OAuthUserProfile profile(String code) {
        var registration = clientRegistration();
        var tokenResponse = exchange(code, registration);
        return profile(tokenResponse, registration);
    }

    protected abstract ClientRegistration clientRegistration();

    protected abstract OAuthUserProfile profile(
            OAuth2AccessTokenResponse tokenResponse, ClientRegistration registration);

    protected Jwt decodeIdToken(String idToken, ClientRegistration registration) {
        var issuerUri = registration.getProviderDetails().getIssuerUri();

        var decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        var issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        var audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audience ->
                                audience != null && audience.contains(registration.getClientId()));

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));

        return decoder.decode(idToken);
    }

    protected String requireIdToken(OAuth2AccessTokenResponse tokenResponse) {
        var idToken = tokenResponse.getAdditionalParameters().get("id_token");

        if (!(idToken instanceof String token) || token.isBlank()) {
            throw new UnauthorizedException();
        }

        return token;
    }

    private OAuth2AccessTokenResponse exchange(String code, ClientRegistration registration) {
        var authorizationRequest =
                OAuth2AuthorizationRequest.authorizationCode()
                        .clientId(registration.getClientId())
                        .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                        .redirectUri(registration.getRedirectUri())
                        .scopes(registration.getScopes())
                        .state("state")
                        .build();

        var authorizationResponse =
                OAuth2AuthorizationResponse.success(code)
                        .redirectUri(registration.getRedirectUri())
                        .state("state")
                        .build();

        var grantRequest =
                new OAuth2AuthorizationCodeGrantRequest(
                        registration,
                        new OAuth2AuthorizationExchange(
                                authorizationRequest, authorizationResponse));

        return tokenClient.getTokenResponse(grantRequest);
    }
}

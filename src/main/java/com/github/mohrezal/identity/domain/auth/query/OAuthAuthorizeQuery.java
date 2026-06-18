package com.github.mohrezal.identity.domain.auth.query;

import com.github.mohrezal.identity.domain.auth.dto.OAuthAuthorizeResponse;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthAuthorizationRequest;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthAuthorizeQueryParams;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProviderRegistry;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedQuery;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthAuthorizeQuery
        extends AuthenticatedQuery<OAuthAuthorizeQueryParams, OAuthAuthorizeResponse> {
    private static final int TOKEN_BYTES = 32;

    private final RedirectValidationService redirectValidationService;
    private final RedisService redisService;
    private final OAuthProviderRegistry providerRegistry;
    private final HashService hashService;

    @Override
    public void validate(OAuthAuthorizeQueryParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
        if (OAuthFlowType.LINK.equals(params.flowType())) {
            getCurrentUser(params);
        }
    }

    @Override
    public OAuthAuthorizeResponse execute(OAuthAuthorizeQueryParams params) {
        validate(params);

        var state = hashService.generateSecureToken(TOKEN_BYTES);
        var correlationId = hashService.generateSecureToken(TOKEN_BYTES);
        var nonce = hashService.generateSecureToken(TOKEN_BYTES);
        var codeVerifier = hashService.generateSecureToken(TOKEN_BYTES);
        var payload =
                new OAuthStatePayload(
                        params.redirectUrl(),
                        params.flowType(),
                        params.providerType(),
                        OAuthFlowType.LINK.equals(params.flowType())
                                ? getCurrentUser(params).getId()
                                : null,
                        correlationId,
                        nonce,
                        codeVerifier);

        redisService.set(RedisKey.OAUTH_STATE, payload, state);

        var authorizationRequest =
                new OAuthAuthorizationRequest(
                        state, nonce, hashService.sha256Base64UrlEncoded(codeVerifier));
        var authorizationUrl =
                providerRegistry
                        .get(params.providerType())
                        .buildAuthorizationUrl(authorizationRequest);

        return new OAuthAuthorizeResponse(authorizationUrl, correlationId);
    }
}

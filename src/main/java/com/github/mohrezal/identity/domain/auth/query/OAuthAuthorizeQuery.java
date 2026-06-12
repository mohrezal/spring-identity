package com.github.mohrezal.identity.domain.auth.query;

import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthAuthorizeQueryParams;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProviderRegistry;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.interfaces.Query;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthAuthorizeQuery implements Query<OAuthAuthorizeQueryParams, String> {

    private final RedirectValidationService redirectValidationService;
    private final RedisService redisService;
    private final OAuthProviderRegistry providerRegistry;

    @Override
    public void validate(OAuthAuthorizeQueryParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
        if (OAuthFlowType.LINK.equals(params.flowType()) && params.userId() == null) {
            throw new UnauthorizedException();
        }
    }

    @Override
    public String execute(OAuthAuthorizeQueryParams params) {
        validate(params);

        var state = UUID.randomUUID().toString();
        var payload =
                new OAuthStatePayload(
                        params.redirectUrl(),
                        params.flowType(),
                        params.providerType(),
                        params.userId());

        redisService.set(RedisKey.OAUTH_STATE, payload, state);

        return providerRegistry.get(params.providerType()).buildAuthorizationUrl(state);
    }
}

package com.github.mohrezal.identity.domain.auth.query.param;

import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import java.util.UUID;

public record OAuthAuthorizeQueryParams(
        OAuthProviderType providerType, OAuthFlowType flowType, String redirectUrl, UUID userId) {}

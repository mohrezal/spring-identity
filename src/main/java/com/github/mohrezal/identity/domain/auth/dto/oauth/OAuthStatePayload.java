package com.github.mohrezal.identity.domain.auth.dto.oauth;

import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import java.util.UUID;

public record OAuthStatePayload(
        String redirectUrl, OAuthFlowType flowType, OAuthProviderType provider, UUID userId) {}

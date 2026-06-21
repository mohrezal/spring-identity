package com.github.mohrezal.identity.domain.authentication.dto.oauth;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;
import java.util.UUID;

public record OAuthStatePayload(
        String redirectUrl,
        OAuthFlowType flowType,
        OAuthProviderType provider,
        UUID userId,
        String correlationId) {}

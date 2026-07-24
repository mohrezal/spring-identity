package com.github.mohrezal.identity.domain.auth.dto;

import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;

public record OAuthCallbackResponse(
        AuthResponse authResponse, String redirectUrl, OAuthFlowType flowType) {}

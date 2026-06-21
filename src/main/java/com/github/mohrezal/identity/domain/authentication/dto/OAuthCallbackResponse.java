package com.github.mohrezal.identity.domain.authentication.dto;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthFlowType;

public record OAuthCallbackResponse(
        AuthResponse authResponse, String redirectUrl, OAuthFlowType flowType) {}

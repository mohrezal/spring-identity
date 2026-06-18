package com.github.mohrezal.identity.domain.auth.dto.oauth;

public record OAuthAuthorizationRequest(String state, String nonce, String codeChallenge) {}

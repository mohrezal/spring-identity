package com.github.mohrezal.identity.domain.auth.dto;

public record OAuthAuthorizeResponse(String authorizationUrl, String correlationId) {}

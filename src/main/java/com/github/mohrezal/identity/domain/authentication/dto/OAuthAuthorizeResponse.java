package com.github.mohrezal.identity.domain.authentication.dto;

public record OAuthAuthorizeResponse(String authorizationUrl, String correlationId) {}

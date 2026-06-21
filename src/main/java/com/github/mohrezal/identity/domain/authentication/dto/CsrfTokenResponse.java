package com.github.mohrezal.identity.domain.authentication.dto;

public record CsrfTokenResponse(String token, String headerName) {}

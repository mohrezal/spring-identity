package com.github.mohrezal.identity.domain.auth.dto;

public record CsrfTokenResponse(String token, String headerName) {}

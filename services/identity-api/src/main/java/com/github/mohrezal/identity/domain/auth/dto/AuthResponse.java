package com.github.mohrezal.identity.domain.auth.dto;

import java.util.UUID;

public record AuthResponse(String accessToken, String refreshToken, UUID sessionId) {

    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, null);
    }
}

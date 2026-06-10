package com.github.mohrezal.identity.domain.auth.command.param;

public record RefreshTokenCommandParams(
        String rawRefreshToken, String ipAddress, String userAgent) {}

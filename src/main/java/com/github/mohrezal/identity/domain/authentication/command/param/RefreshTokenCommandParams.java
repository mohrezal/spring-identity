package com.github.mohrezal.identity.domain.authentication.command.param;

public record RefreshTokenCommandParams(
        String rawRefreshToken, String ipAddress, String userAgent) {}

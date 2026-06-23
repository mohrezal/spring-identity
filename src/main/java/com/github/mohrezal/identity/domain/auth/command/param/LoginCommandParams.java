package com.github.mohrezal.identity.domain.auth.command.param;

import com.github.mohrezal.identity.domain.auth.dto.LoginRequest;

public record LoginCommandParams(LoginRequest request, String ipAddress, String userAgent) {}

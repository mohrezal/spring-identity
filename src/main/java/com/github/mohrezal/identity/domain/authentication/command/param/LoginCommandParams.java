package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.domain.authentication.dto.LoginRequest;

public record LoginCommandParams(LoginRequest request, String ipAddress, String userAgent) {}

package com.github.mohrezal.identity.domain.user.command.param;

import com.github.mohrezal.identity.domain.user.dto.RegisterRequest;

public record RegisterCommandParams(RegisterRequest request, String redirectUrl) {}

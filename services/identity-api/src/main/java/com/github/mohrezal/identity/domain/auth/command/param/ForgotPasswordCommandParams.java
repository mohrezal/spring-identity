package com.github.mohrezal.identity.domain.auth.command.param;

import com.github.mohrezal.identity.domain.auth.dto.ForgotPasswordRequest;

public record ForgotPasswordCommandParams(ForgotPasswordRequest request, String redirectUrl) {}

package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.domain.authentication.dto.ForgotPasswordRequest;

public record ForgotPasswordCommandParams(ForgotPasswordRequest request, String redirectUrl) {}

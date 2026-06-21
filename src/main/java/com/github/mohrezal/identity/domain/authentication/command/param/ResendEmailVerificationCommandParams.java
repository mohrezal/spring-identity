package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.domain.authentication.dto.ResendEmailVerificationRequest;

public record ResendEmailVerificationCommandParams(
        ResendEmailVerificationRequest request, String redirectUrl) {}

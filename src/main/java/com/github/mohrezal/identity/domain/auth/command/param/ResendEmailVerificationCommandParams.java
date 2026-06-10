package com.github.mohrezal.identity.domain.auth.command.param;

import com.github.mohrezal.identity.domain.auth.dto.ResendEmailVerificationRequest;

public record ResendEmailVerificationCommandParams(
        ResendEmailVerificationRequest request, String redirectUrl) {}

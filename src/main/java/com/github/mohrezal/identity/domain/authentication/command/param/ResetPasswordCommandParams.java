package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.domain.authentication.dto.ResetPasswordRequest;

public record ResetPasswordCommandParams(ResetPasswordRequest request, String redirectUrl) {}

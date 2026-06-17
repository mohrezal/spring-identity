package com.github.mohrezal.identity.domain.auth.command.param;

import com.github.mohrezal.identity.domain.auth.dto.ResetPasswordRequest;

public record ResetPasswordCommandParams(ResetPasswordRequest request, String redirectUrl) {}

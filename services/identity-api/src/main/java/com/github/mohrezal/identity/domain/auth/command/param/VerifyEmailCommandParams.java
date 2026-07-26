package com.github.mohrezal.identity.domain.auth.command.param;

import java.util.UUID;

public record VerifyEmailCommandParams(UUID token, String redirectUrl) {}

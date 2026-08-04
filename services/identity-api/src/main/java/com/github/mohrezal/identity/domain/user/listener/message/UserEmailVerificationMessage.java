package com.github.mohrezal.identity.domain.user.listener.message;

import java.util.UUID;

public record UserEmailVerificationMessage(UUID userId, String to, String activationUrl) {}

package com.github.mohrezal.identity.domain.auth.listener.message;

import java.util.UUID;

public record PasswordResetEmailMessage(UUID userId, String to, String resetUrl) {}

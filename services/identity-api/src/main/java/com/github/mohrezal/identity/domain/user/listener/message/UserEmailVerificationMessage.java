package com.github.mohrezal.identity.domain.user.listener.message;

public record UserEmailVerificationMessage(String to, String activationUrl) {}

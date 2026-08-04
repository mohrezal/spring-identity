package com.github.mohrezal.identity.domain.auth.dto;

import java.util.UUID;

public record VerifyEmailResult(UUID userId, String email) {}

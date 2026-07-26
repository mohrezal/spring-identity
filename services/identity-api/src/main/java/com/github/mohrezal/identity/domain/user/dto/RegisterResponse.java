package com.github.mohrezal.identity.domain.user.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String message) {}

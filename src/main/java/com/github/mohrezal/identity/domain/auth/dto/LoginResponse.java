package com.github.mohrezal.identity.domain.auth.dto;

import com.github.mohrezal.identity.domain.user.dto.UserSummary;

public record LoginResponse(AuthResponse authResponse, UserSummary userSummary) {}

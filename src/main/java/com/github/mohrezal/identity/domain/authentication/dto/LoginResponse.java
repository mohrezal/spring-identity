package com.github.mohrezal.identity.domain.authentication.dto;

import com.github.mohrezal.identity.domain.user.dto.UserSummary;

public record LoginResponse(AuthResponse authResponse, UserSummary userSummary) {}

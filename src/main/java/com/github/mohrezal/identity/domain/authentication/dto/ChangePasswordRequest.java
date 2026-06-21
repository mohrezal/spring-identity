package com.github.mohrezal.identity.domain.authentication.dto;

import com.github.mohrezal.identity.shared.constant.RegexPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN)
                String currentPassword,
        @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN)
                String newPassword) {}

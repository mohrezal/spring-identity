package com.github.mohrezal.identity.domain.auth.dto;

import com.github.mohrezal.identity.shared.constant.RegexPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN)
                String password) {}

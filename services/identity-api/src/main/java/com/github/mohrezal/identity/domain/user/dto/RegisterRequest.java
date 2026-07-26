package com.github.mohrezal.identity.domain.user.dto;

import com.github.mohrezal.identity.shared.constant.RegexPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 50) @Pattern(regexp = RegexPatterns.NAME_PATTERN)
                String firstName,
        @NotBlank @Size(min = 2, max = 50) @Pattern(regexp = RegexPatterns.NAME_PATTERN)
                String lastName,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN)
                String password) {}

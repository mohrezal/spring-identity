package com.github.mohrezal.identity.domain.auth.dto;

import com.github.mohrezal.identity.shared.constant.RegexPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ResetPasswordRequest(
        @NotNull UUID token,
        @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN)
                String newPassword) {}

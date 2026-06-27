package com.github.mohrezal.identity.domain.privilege.dto;

import com.github.mohrezal.identity.shared.constant.RegexPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = RegexPatterns.KEY_PATTERN) String key,
        @NotBlank @Size(max = 150) String name,
        @NotNull Set<@NotNull UUID> permissionIds) {}

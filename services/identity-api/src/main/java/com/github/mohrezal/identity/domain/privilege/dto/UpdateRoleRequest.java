package com.github.mohrezal.identity.domain.privilege.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull Boolean enabled,
        @NotNull Set<@NotNull UUID> permissionIds) {}

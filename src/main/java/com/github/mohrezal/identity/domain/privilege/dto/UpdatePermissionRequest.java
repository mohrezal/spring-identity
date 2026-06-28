package com.github.mohrezal.identity.domain.privilege.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(
        @NotBlank @Size(max = 150) String name, @NotNull Boolean enabled) {}

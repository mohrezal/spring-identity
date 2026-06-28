package com.github.mohrezal.identity.domain.privilege.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record UpdateUserRolesRequest(@NotNull Set<@NotNull UUID> roleIds) {}

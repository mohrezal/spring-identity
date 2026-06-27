package com.github.mohrezal.identity.domain.privilege.dto;

import java.util.List;
import java.util.UUID;

public record RoleSummary(
        UUID id, String key, String name, Boolean enabled, List<PermissionSummary> permissions) {}

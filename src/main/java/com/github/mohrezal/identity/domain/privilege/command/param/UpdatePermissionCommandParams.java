package com.github.mohrezal.identity.domain.privilege.command.param;

import com.github.mohrezal.identity.domain.privilege.dto.UpdatePermissionRequest;
import java.util.UUID;

public record UpdatePermissionCommandParams(UUID permissionId, UpdatePermissionRequest request) {}

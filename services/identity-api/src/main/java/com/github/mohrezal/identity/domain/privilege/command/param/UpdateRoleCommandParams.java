package com.github.mohrezal.identity.domain.privilege.command.param;

import com.github.mohrezal.identity.domain.privilege.dto.UpdateRoleRequest;
import java.util.UUID;

public record UpdateRoleCommandParams(UUID roleId, UpdateRoleRequest request) {}

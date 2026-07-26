package com.github.mohrezal.identity.domain.privilege.command.param;

import com.github.mohrezal.identity.domain.privilege.dto.UpdateUserRolesRequest;
import java.util.UUID;

public record UpdateUserRolesCommandParams(UUID userId, UpdateUserRolesRequest request) {}

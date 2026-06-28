package com.github.mohrezal.identity.domain.privilege.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.privilege.command.CreateRoleCommand;
import com.github.mohrezal.identity.domain.privilege.command.UpdateRoleCommand;
import com.github.mohrezal.identity.domain.privilege.command.UpdateUserRolesCommand;
import com.github.mohrezal.identity.domain.privilege.command.param.CreateRoleCommandParams;
import com.github.mohrezal.identity.domain.privilege.command.param.UpdateRoleCommandParams;
import com.github.mohrezal.identity.domain.privilege.command.param.UpdateUserRolesCommandParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.CreateRoleRequest;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.dto.UpdateRoleRequest;
import com.github.mohrezal.identity.domain.privilege.dto.UpdateUserRolesRequest;
import com.github.mohrezal.identity.domain.privilege.query.GetRoleQuery;
import com.github.mohrezal.identity.domain.privilege.query.GetRolesQuery;
import com.github.mohrezal.identity.domain.privilege.query.param.GetRoleQueryParams;
import com.github.mohrezal.identity.domain.privilege.query.param.GetRolesQueryParams;
import com.github.mohrezal.identity.shared.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Privilege.ROLES)
@RequiredArgsConstructor
@Tag(name = "Privileges")
public class RoleController {

    private final GetRolesQuery getRolesQuery;
    private final GetRoleQuery getRoleQuery;
    private final CreateRoleCommand createRoleCommand;
    private final UpdateRoleCommand updateRoleCommand;
    private final UpdateUserRolesCommand updateUserRolesCommand;

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_ROLES_READ)
    @GetMapping
    public ResponseEntity<List<RoleSummary>> roles() {
        var response = getRolesQuery.execute(new GetRolesQueryParams());
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_ROLES_READ)
    @GetMapping(RouteConstants.Privilege.ROLE)
    public ResponseEntity<RoleSummary> role(@PathVariable UUID id) {
        var response = getRoleQuery.execute(new GetRoleQueryParams(id));
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_ROLES_CREATE)
    @PostMapping
    public ResponseEntity<RoleSummary> create(@Valid @RequestBody CreateRoleRequest body) {
        var response = createRoleCommand.execute(new CreateRoleCommandParams(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_ROLES_UPDATE)
    @PutMapping(RouteConstants.Privilege.ROLE)
    public ResponseEntity<RoleSummary> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest body) {
        var response = updateRoleCommand.execute(new UpdateRoleCommandParams(id, body));
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES)
    @PutMapping(RouteConstants.Privilege.ROLE_ASSIGNMENTS)
    public ResponseEntity<List<RoleSummary>> updateUserRoles(
            @PathVariable UUID userId, @Valid @RequestBody UpdateUserRolesRequest body) {
        var response =
                updateUserRolesCommand.execute(new UpdateUserRolesCommandParams(userId, body));
        return ResponseEntity.ok(response);
    }
}

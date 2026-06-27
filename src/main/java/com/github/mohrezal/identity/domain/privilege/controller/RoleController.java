package com.github.mohrezal.identity.domain.privilege.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.privilege.command.CreateRoleCommand;
import com.github.mohrezal.identity.domain.privilege.command.param.CreateRoleCommandParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.CreateRoleRequest;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.query.GetRolesQuery;
import com.github.mohrezal.identity.domain.privilege.query.param.GetRolesQueryParams;
import com.github.mohrezal.identity.shared.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Privilege.ROLES)
@RequiredArgsConstructor
@Tag(name = "Privileges")
public class RoleController {

    private final GetRolesQuery getRolesQuery;
    private final CreateRoleCommand createRoleCommand;

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_ROLES_READ)
    @GetMapping
    public ResponseEntity<List<RoleSummary>> roles() {
        var response = getRolesQuery.execute(new GetRolesQueryParams());
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_ROLES_CREATE)
    @PostMapping
    public ResponseEntity<RoleSummary> create(@Valid @RequestBody CreateRoleRequest body) {
        var response = createRoleCommand.execute(new CreateRoleCommandParams(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

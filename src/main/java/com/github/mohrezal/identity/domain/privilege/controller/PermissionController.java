package com.github.mohrezal.identity.domain.privilege.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.query.GetPermissionQuery;
import com.github.mohrezal.identity.domain.privilege.query.GetPermissionsQuery;
import com.github.mohrezal.identity.domain.privilege.query.param.GetPermissionQueryParams;
import com.github.mohrezal.identity.domain.privilege.query.param.GetPermissionsQueryParams;
import com.github.mohrezal.identity.shared.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Privilege.PERMISSIONS)
@RequiredArgsConstructor
@Tag(name = "Privileges")
public class PermissionController {

    private final GetPermissionsQuery getPermissionsQuery;
    private final GetPermissionQuery getPermissionQuery;

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_READ)
    @GetMapping
    public ResponseEntity<List<PermissionSummary>> permissions() {
        var response = getPermissionsQuery.execute(new GetPermissionsQueryParams());
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_READ)
    @GetMapping(RouteConstants.Privilege.PERMISSION)
    public ResponseEntity<PermissionSummary> permission(@PathVariable UUID id) {
        var response = getPermissionQuery.execute(new GetPermissionQueryParams(id));
        return ResponseEntity.ok(response);
    }
}

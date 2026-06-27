package com.github.mohrezal.identity.domain.privilege.mapper;

import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.RolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleMapper {

    RoleSummary toSummary(Role role);

    @Mapping(target = "id", source = "permission.id")
    @Mapping(target = "key", source = "permission.key")
    @Mapping(target = "name", source = "permission.name")
    @Mapping(target = "service", source = "permission.service")
    @Mapping(target = "enabled", source = "permission.enabled")
    PermissionSummary toPermissionSummary(RolePermission rolePermission);
}

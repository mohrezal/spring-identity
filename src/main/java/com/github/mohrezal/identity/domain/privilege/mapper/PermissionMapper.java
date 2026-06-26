package com.github.mohrezal.identity.domain.privilege.mapper;

import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PermissionMapper {

    PermissionSummary toSummary(Permission permission);
}

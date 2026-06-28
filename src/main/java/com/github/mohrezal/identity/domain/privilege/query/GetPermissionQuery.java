package com.github.mohrezal.identity.domain.privilege.query;

import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.exception.type.PermissionNotFoundException;
import com.github.mohrezal.identity.domain.privilege.mapper.PermissionMapper;
import com.github.mohrezal.identity.domain.privilege.query.param.GetPermissionQueryParams;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.shared.interfaces.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPermissionQuery implements Query<GetPermissionQueryParams, PermissionSummary> {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public PermissionSummary execute(GetPermissionQueryParams params) {
        return permissionRepository
                .findById(params.permissionId())
                .map(permissionMapper::toSummary)
                .orElseThrow(PermissionNotFoundException::new);
    }
}

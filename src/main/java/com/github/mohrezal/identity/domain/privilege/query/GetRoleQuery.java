package com.github.mohrezal.identity.domain.privilege.query;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleNotFoundException;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.query.param.GetRoleQueryParams;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.shared.interfaces.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRoleQuery implements Query<GetRoleQueryParams, RoleSummary> {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public RoleSummary execute(GetRoleQueryParams params, AuditRequestContext auditRequestContext) {
        return roleRepository
                .findByIdWithPermissions(params.roleId())
                .map(roleMapper::toSummary)
                .orElseThrow(RoleNotFoundException::new);
    }
}

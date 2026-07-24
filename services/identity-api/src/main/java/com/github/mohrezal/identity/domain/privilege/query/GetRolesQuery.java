package com.github.mohrezal.identity.domain.privilege.query;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.query.param.GetRolesQueryParams;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.shared.interfaces.Query;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRolesQuery implements Query<GetRolesQueryParams, List<RoleSummary>> {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleSummary> execute(
            GetRolesQueryParams params, AuditRequestContext auditRequestContext) {
        return roleRepository.findAllWithPermissions().stream().map(roleMapper::toSummary).toList();
    }
}

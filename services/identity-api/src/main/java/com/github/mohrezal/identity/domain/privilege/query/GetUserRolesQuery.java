package com.github.mohrezal.identity.domain.privilege.query;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.query.param.GetUserRolesQueryParams;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.interfaces.Query;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserRolesQuery implements Query<GetUserRolesQueryParams, List<RoleSummary>> {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleSummary> execute(
            GetUserRolesQueryParams params, AuditRequestContext auditRequestContext) {
        if (!userRepository.existsById(params.userId())) {
            throw new UserNotFoundException();
        }

        return userRoleRepository.findAllByUser_Id(params.userId()).stream()
                .map(userRole -> roleMapper.toSummary(userRole.getRole()))
                .toList();
    }
}

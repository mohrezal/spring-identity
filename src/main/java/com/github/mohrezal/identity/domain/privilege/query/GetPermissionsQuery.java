package com.github.mohrezal.identity.domain.privilege.query;

import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.mapper.PermissionMapper;
import com.github.mohrezal.identity.domain.privilege.query.param.GetPermissionsQueryParams;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.shared.interfaces.Query;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPermissionsQuery
        implements Query<GetPermissionsQueryParams, List<PermissionSummary>> {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionSummary> execute(GetPermissionsQueryParams params) {
        return permissionRepository.findAllByOrderByServiceAscKeyAsc().stream()
                .map(permissionMapper::toSummary)
                .toList();
    }
}

package com.github.mohrezal.identity.domain.privilege.query;

import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummaryCache;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.query.param.GetRolesQueryParams;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.interfaces.Query;
import com.github.mohrezal.identity.shared.redis.RedisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRolesQuery implements Query<GetRolesQueryParams, List<RoleSummary>> {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RedisService redisService;

    @Override
    @Transactional(readOnly = true)
    public List<RoleSummary> execute(GetRolesQueryParams params) {
        return redisService
                .get(RedisKey.PRIVILEGE_ROLES, RoleSummaryCache.class)
                .map(RoleSummaryCache::roles)
                .orElseGet(
                        () -> {
                            var roles =
                                    roleRepository.findAllWithPermissions().stream()
                                            .map(roleMapper::toSummary)
                                            .toList();

                            redisService.set(RedisKey.PRIVILEGE_ROLES, new RoleSummaryCache(roles));
                            return roles;
                        });
    }
}

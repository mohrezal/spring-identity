package com.github.mohrezal.identity.domain.privilege.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.privilege.command.param.UpdatePermissionCommandParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.exception.type.PermissionNotFoundException;
import com.github.mohrezal.identity.domain.privilege.exception.type.ProtectedPermissionCannotBeDisabledException;
import com.github.mohrezal.identity.domain.privilege.mapper.PermissionMapper;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.privilege.service.UserPrivilegeVersionService;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePermissionCommand
        implements Command<UpdatePermissionCommandParams, PermissionSummary> {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserPrivilegeVersionService userPrivilegeVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionSummary execute(
            UpdatePermissionCommandParams params, AuditRequestContext auditRequestContext) {
        var permission =
                permissionRepository
                        .findById(params.permissionId())
                        .orElseThrow(PermissionNotFoundException::new);
        var request = params.request();

        if (Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_UPDATE.equals(permission.getKey())
                && !request.enabled()) {
            throw new ProtectedPermissionCannotBeDisabledException();
        }

        var enabledChanged = !Objects.equals(permission.getEnabled(), request.enabled());
        permission.setName(request.name());
        permission.setEnabled(request.enabled());

        var savedPermission = permissionRepository.save(permission);
        if (enabledChanged) {
            userRoleRepository.findUserIdsByPermissionId(savedPermission.getId()).stream()
                    .map(
                            userId ->
                                    userRepository
                                            .findById(userId)
                                            .orElseThrow(UserNotFoundException::new))
                    .forEach(userPrivilegeVersionService::increment);
        }
        log.info(
                "Permission updated. permissionId={}, key={}",
                savedPermission.getId(),
                savedPermission.getKey());
        return permissionMapper.toSummary(savedPermission);
    }
}

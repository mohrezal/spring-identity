package com.github.mohrezal.identity.domain.privilege.command;

import com.github.mohrezal.identity.domain.privilege.command.param.UpdatePermissionCommandParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.PermissionSummary;
import com.github.mohrezal.identity.domain.privilege.exception.type.PermissionNotFoundException;
import com.github.mohrezal.identity.domain.privilege.exception.type.ProtectedPermissionCannotBeDisabledException;
import com.github.mohrezal.identity.domain.privilege.mapper.PermissionMapper;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionSummary execute(UpdatePermissionCommandParams params) {
        var permission =
                permissionRepository
                        .findById(params.permissionId())
                        .orElseThrow(PermissionNotFoundException::new);
        var request = params.request();

        if (Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_UPDATE.equals(permission.getKey())
                && !request.enabled()) {
            throw new ProtectedPermissionCannotBeDisabledException();
        }

        permission.setName(request.name());
        permission.setEnabled(request.enabled());

        var savedPermission = permissionRepository.save(permission);
        log.info(
                "Permission updated. permissionId={}, key={}",
                savedPermission.getId(),
                savedPermission.getKey());
        return permissionMapper.toSummary(savedPermission);
    }
}

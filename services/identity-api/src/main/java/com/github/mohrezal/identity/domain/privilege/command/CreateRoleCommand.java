package com.github.mohrezal.identity.domain.privilege.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.privilege.command.param.CreateRoleCommandParams;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.exception.type.PermissionNotFoundException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleKeyAlreadyExistsException;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.RolePermission;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRoleCommand implements Command<CreateRoleCommandParams, RoleSummary> {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public void validate(CreateRoleCommandParams params) {
        if (roleRepository.existsByKey(params.request().key())) {
            throw new RoleKeyAlreadyExistsException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummary execute(
            CreateRoleCommandParams params, AuditRequestContext auditRequestContext) {
        validate(params);

        var request = params.request();
        var role = Role.builder().key(request.key()).name(request.name()).build();
        var permissions = permissionRepository.findAllById(request.permissionIds());

        if (permissions.size() != request.permissionIds().size()) {
            throw new PermissionNotFoundException();
        }

        permissions.forEach(
                permission ->
                        role.getPermissions()
                                .add(
                                        RolePermission.builder()
                                                .role(role)
                                                .permission(permission)
                                                .build()));

        try {
            var savedRole = roleRepository.saveAndFlush(role);
            log.info("Role created. roleId={}, key={}", savedRole.getId(), savedRole.getKey());
            return roleMapper.toSummary(savedRole);
        } catch (DataIntegrityViolationException exception) {
            throw new RoleKeyAlreadyExistsException();
        }
    }
}

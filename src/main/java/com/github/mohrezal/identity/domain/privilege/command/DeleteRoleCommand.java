package com.github.mohrezal.identity.domain.privilege.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.privilege.command.param.DeleteRoleCommandParams;
import com.github.mohrezal.identity.domain.privilege.exception.type.ConfiguredRoleCannotBeDeletedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleAssignedToUsersException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleNotFoundException;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteRoleCommand implements Command<DeleteRoleCommandParams, Void> {

    private final ApplicationProperties applicationProperties;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(DeleteRoleCommandParams params, AuditRequestContext auditRequestContext) {
        var role = roleRepository.findById(params.roleId()).orElseThrow(RoleNotFoundException::new);
        var configuredRoles = applicationProperties.privilege().role();
        var configuredRoleKeys =
                Set.of(configuredRoles.owner().key(), configuredRoles.user().key());

        if (configuredRoleKeys.contains(role.getKey())) {
            throw new ConfiguredRoleCannotBeDeletedException();
        }

        if (userRoleRepository.existsByRole(role)) {
            throw new RoleAssignedToUsersException();
        }

        roleRepository.delete(role);
        log.info("Role deleted. roleId={}, key={}", role.getId(), role.getKey());
        return null;
    }
}

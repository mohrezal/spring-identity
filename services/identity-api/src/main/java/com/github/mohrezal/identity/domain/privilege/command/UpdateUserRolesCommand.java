package com.github.mohrezal.identity.domain.privilege.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.privilege.command.param.UpdateUserRolesCommandParams;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.exception.type.LastOwnerRoleCannotBeRemovedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleNotFoundException;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.UserRole;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.privilege.service.UserPrivilegeVersionService;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserRolesCommand
        implements Command<UpdateUserRolesCommandParams, List<RoleSummary>> {

    private final ApplicationProperties applicationProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;
    private final UserPrivilegeVersionService userPrivilegeVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RoleSummary> execute(
            UpdateUserRolesCommandParams params, AuditRequestContext auditRequestContext) {
        var user = userRepository.findById(params.userId()).orElseThrow(UserNotFoundException::new);
        var roleIds = params.request().roleIds();
        List<Role> roles =
                roleIds.isEmpty() ? List.of() : roleRepository.findAllByIdWithPermissions(roleIds);

        if (roles.size() != roleIds.size()) {
            throw new RoleNotFoundException();
        }

        var configuredOwnerRoleKey = applicationProperties.privilege().role().owner().key();
        var ownerRole =
                roleRepository
                        .findByKeyForUpdate(configuredOwnerRoleKey)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Configured owner role not found: "
                                                        + configuredOwnerRoleKey));
        var removesOwnerRole =
                !roleIds.contains(ownerRole.getId())
                        && userRoleRepository.existsByUserAndRole(user, ownerRole);

        if (removesOwnerRole && userRoleRepository.countByRole(ownerRole) == 1) {
            throw new LastOwnerRoleCannotBeRemovedException();
        }

        var existingAssignments = userRoleRepository.findAllByUser(user);
        var existingRoleIds =
                existingAssignments.stream()
                        .map(userRole -> userRole.getRole().getId())
                        .collect(Collectors.toSet());

        var assignmentsToDelete =
                existingAssignments.stream()
                        .filter(userRole -> !roleIds.contains(userRole.getRole().getId()))
                        .toList();
        var assignmentsToCreate =
                roles.stream()
                        .filter(role -> !existingRoleIds.contains(role.getId()))
                        .map(role -> UserRole.builder().user(user).role(role).build())
                        .toList();

        userRoleRepository.deleteAll(assignmentsToDelete);
        userRoleRepository.saveAll(assignmentsToCreate);

        if (!assignmentsToDelete.isEmpty() || !assignmentsToCreate.isEmpty()) {
            userPrivilegeVersionService.increment(user);
        }

        log.info("User roles updated. userId={}, roleCount={}", user.getId(), roles.size());
        return roles.stream().map(roleMapper::toSummary).toList();
    }
}

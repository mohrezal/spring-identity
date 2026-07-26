package com.github.mohrezal.identity.domain.privilege.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.privilege.command.param.UpdateRoleCommandParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.RoleSummary;
import com.github.mohrezal.identity.domain.privilege.dto.UpdateRoleRequest;
import com.github.mohrezal.identity.domain.privilege.exception.type.OwnerRoleCannotBeUpdatedException;
import com.github.mohrezal.identity.domain.privilege.mapper.RoleMapper;
import com.github.mohrezal.identity.domain.privilege.model.Permission;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.RolePermission;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateRoleCommandTest {

    private static final String OWNER_ROLE_KEY = "owner";
    private static final String ROLE_KEY = "support";
    private static final ApplicationProperties.Privilege PRIVILEGE_PROPERTIES =
            new ApplicationProperties.Privilege(
                    new ApplicationProperties.Privilege.Role(
                            new ApplicationProperties.Privilege.Properties(OWNER_ROLE_KEY, "Owner"),
                            new ApplicationProperties.Privilege.Properties("user", "User")));

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private UpdateRoleCommand command;

    @Test
    void execute_whenPermissionsChange_reconcilesAssignmentsWithoutChangingKey() {
        var roleId = UUID.randomUUID();
        var retainedPermission =
                Permission.builder()
                        .id(UUID.randomUUID())
                        .key(Permissions.IDENTITY_AUTH_SESSIONS_READ)
                        .build();
        var stalePermission =
                Permission.builder()
                        .id(UUID.randomUUID())
                        .key(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE)
                        .build();
        var newPermission =
                Permission.builder()
                        .id(UUID.randomUUID())
                        .key(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE_ALL)
                        .build();
        var role = Role.builder().id(roleId).key(ROLE_KEY).name("Old name").enabled(true).build();
        var retainedAssignment =
                RolePermission.builder().role(role).permission(retainedPermission).build();
        var staleAssignment =
                RolePermission.builder().role(role).permission(stalePermission).build();
        role.getPermissions().add(retainedAssignment);
        role.getPermissions().add(staleAssignment);
        var request =
                new UpdateRoleRequest(
                        "Support operators",
                        false,
                        Set.of(retainedPermission.getId(), newPermission.getId()));
        var params = new UpdateRoleCommandParams(roleId, request);
        var summary = new RoleSummary(roleId, ROLE_KEY, "Support operators", false, List.of());
        when(applicationProperties.privilege()).thenReturn(PRIVILEGE_PROPERTIES);
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(request.permissionIds()))
                .thenReturn(List.of(retainedPermission, newPermission));
        when(roleRepository.saveAndFlush(role)).thenReturn(role);
        when(roleMapper.toSummary(role)).thenReturn(summary);

        var result = command.execute(params, null);

        assertThat(result).isSameAs(summary);
        assertThat(role.getKey()).isEqualTo(ROLE_KEY);
        assertThat(role.getName()).isEqualTo("Support operators");
        assertThat(role.getEnabled()).isFalse();
        assertThat(role.getPermissions())
                .extracting(rolePermission -> rolePermission.getPermission().getId())
                .containsExactlyInAnyOrder(retainedPermission.getId(), newPermission.getId());
        assertThat(role.getPermissions())
                .filteredOn(
                        rolePermission ->
                                rolePermission
                                        .getPermission()
                                        .getId()
                                        .equals(retainedPermission.getId()))
                .singleElement()
                .isSameAs(retainedAssignment);
        verify(roleRepository).saveAndFlush(role);
        verify(roleMapper).toSummary(role);
    }

    @Test
    void execute_whenRoleIsOwner_rejectsBeforeMutation() {
        var roleId = UUID.randomUUID();
        var role =
                Role.builder().id(roleId).key(OWNER_ROLE_KEY).name("Owner").enabled(true).build();
        var request = new UpdateRoleRequest("Changed owner", false, Set.of());
        var params = new UpdateRoleCommandParams(roleId, request);
        when(applicationProperties.privilege()).thenReturn(PRIVILEGE_PROPERTIES);
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> command.execute(params, null))
                .isInstanceOf(OwnerRoleCannotBeUpdatedException.class);
        assertThat(role.getName()).isEqualTo("Owner");
        assertThat(role.getEnabled()).isTrue();
        verify(roleRepository, never()).saveAndFlush(any());
        verifyNoInteractions(permissionRepository, roleMapper);
    }
}

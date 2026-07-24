package com.github.mohrezal.identity.integration.privilege;

import static com.github.mohrezal.identity.support.assertion.ErrorResponseAssertions.errorName;
import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.UpdatePermissionRequest;
import com.github.mohrezal.identity.domain.privilege.dto.UpdateUserRolesRequest;
import com.github.mohrezal.identity.domain.privilege.exception.type.ConfiguredRoleCannotBeDeletedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.LastOwnerRoleCannotBeRemovedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.ProtectedPermissionCannotBeDisabledException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleAssignedToUsersException;
import com.github.mohrezal.identity.domain.privilege.model.Permission;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.UserRole;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Transactional
class PrivilegeInvariantIT extends IntegrationTestSupport {

    private static final String PERMISSIONS_PATH = RouteConstants.Privilege.PERMISSIONS;
    private static final String ROLES_PATH = RouteConstants.Privilege.ROLES;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void updatePermission_rejectsDisablingTheProtectedPermission() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var protectedPermission =
                permissionRepository.saveAndFlush(
                        Permission.builder()
                                .key(Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_UPDATE)
                                .name("Update privilege permissions")
                                .service("identity")
                                .build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_UPDATE));
        var protectedPermissionPath =
                RouteConstants.build(PERMISSIONS_PATH, protectedPermission.getId().toString());

        mockMvc.perform(
                        put(protectedPermissionPath)
                                .with(csrf())
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsBytes(
                                                new UpdatePermissionRequest(
                                                        "Update privilege permissions", false))))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        errorName(
                                                new ProtectedPermissionCannotBeDisabledException())));

        var persistedPermission =
                permissionRepository.findById(protectedPermission.getId()).orElseThrow();
        assertThat(persistedPermission.getEnabled()).isTrue();
        assertThat(persistedPermission.getName()).isEqualTo("Update privilege permissions");
    }

    @Test
    void deleteRole_rejectsConfiguredUserRole() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var configuredUserRole = applicationProperties.privilege().role().user();
        var userRole =
                roleRepository
                        .findByKey(configuredUserRole.key())
                        .orElseGet(
                                () ->
                                        roleRepository.saveAndFlush(
                                                Role.builder()
                                                        .key(configuredUserRole.key())
                                                        .name(configuredUserRole.name())
                                                        .build()));
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_PRIVILEGE_ROLES_DELETE));
        var userRolePath = RouteConstants.build(ROLES_PATH, userRole.getId().toString());

        mockMvc.perform(
                        delete(userRolePath)
                                .with(csrf())
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value(errorName(new ConfiguredRoleCannotBeDeletedException())));

        assertThat(roleRepository.findById(userRole.getId())).isPresent();
    }

    @Test
    void deleteRole_rejectsRoleAssignedToUsers() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var supportRole =
                roleRepository
                        .findByKey("support")
                        .orElseGet(
                                () ->
                                        roleRepository.saveAndFlush(
                                                Role.builder()
                                                        .key("support")
                                                        .name("Support operators")
                                                        .build()));
        userRoleRepository.saveAndFlush(UserRole.builder().user(user).role(supportRole).build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_PRIVILEGE_ROLES_DELETE));
        var supportRolePath = RouteConstants.build(ROLES_PATH, supportRole.getId().toString());

        mockMvc.perform(
                        delete(supportRolePath)
                                .with(csrf())
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(errorName(new RoleAssignedToUsersException())));

        assertThat(roleRepository.findById(supportRole.getId())).isPresent();
        assertThat(userRoleRepository.existsByUserAndRole(user, supportRole)).isTrue();
    }

    @Test
    void updateUserRoles_rejectsRemovingTheLastOwnerRoleAssignment() throws Exception {
        var owner =
                userRepository.saveAndFlush(
                        User.builder()
                                .email(applicationProperties.owner().email())
                                .firstName("Test")
                                .lastName("Owner")
                                .build());
        var configuredOwnerRole = applicationProperties.privilege().role().owner();
        var ownerRole =
                roleRepository
                        .findByKey(configuredOwnerRole.key())
                        .orElseGet(
                                () ->
                                        roleRepository.saveAndFlush(
                                                Role.builder()
                                                        .key(configuredOwnerRole.key())
                                                        .name(configuredOwnerRole.name())
                                                        .build()));
        userRoleRepository.saveAndFlush(UserRole.builder().user(owner).role(ownerRole).build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        owner.getId(), List.of(Permissions.IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES));
        var ownerAssignmentsPath =
                RouteConstants.build(
                        ROLES_PATH,
                        RouteConstants.Privilege.ROLE_ASSIGNMENTS.replace(
                                "{userId}", owner.getId().toString()));

        mockMvc.perform(
                        put(ownerAssignmentsPath)
                                .with(csrf())
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsBytes(
                                                new UpdateUserRolesRequest(Set.of()))))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value(errorName(new LastOwnerRoleCannotBeRemovedException())));

        assertThat(userRoleRepository.existsByUserAndRole(owner, ownerRole)).isTrue();
    }
}

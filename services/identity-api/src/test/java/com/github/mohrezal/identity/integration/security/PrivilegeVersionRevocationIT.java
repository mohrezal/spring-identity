package com.github.mohrezal.identity.integration.security;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.dto.UpdateUserRolesRequest;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.UserRole;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Transactional
class PrivilegeVersionRevocationIT extends IntegrationTestSupport {

    private static final String SESSIONS_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.SESSIONS);

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
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void privilegedEndpoint_rejectsAccessTokenAfterUserRolesAreChanged() throws Exception {
        var configuredOwnerRole = applicationProperties.privilege().role().owner();
        roleRepository
                .findByKey(configuredOwnerRole.key())
                .orElseGet(
                        () ->
                                roleRepository.saveAndFlush(
                                        Role.builder()
                                                .key(configuredOwnerRole.key())
                                                .name(configuredOwnerRole.name())
                                                .build()));

        var admin =
                userRepository.saveAndFlush(
                        User.builder()
                                .email("admin-" + UUID.randomUUID() + "@client.test")
                                .firstName("Admin")
                                .lastName("User")
                                .build());
        var victim =
                userRepository.saveAndFlush(
                        User.builder()
                                .email(UUID.randomUUID() + "-" + EMAIL)
                                .firstName("Victim")
                                .lastName("User")
                                .build());
        var role =
                roleRepository.saveAndFlush(
                        Role.builder()
                                .key("privilege-version-revocation-" + UUID.randomUUID())
                                .name("Temporary role")
                                .build());
        userRoleRepository.saveAndFlush(UserRole.builder().user(victim).role(role).build());

        var victimAccessToken =
                jwtTokenProvider.createAccessToken(
                        victim.getId(),
                        List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ),
                        victim.getPrivilegeVersion());
        var adminAccessToken =
                jwtTokenProvider.createAccessToken(
                        admin.getId(),
                        List.of(Permissions.IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES),
                        admin.getPrivilegeVersion());

        mockMvc.perform(
                        get(SESSIONS_PATH)
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, victimAccessToken)))
                .andExpect(status().isOk());

        var assignmentsPath =
                RouteConstants.build(
                        RouteConstants.Privilege.ROLES,
                        RouteConstants.Privilege.ROLE_ASSIGNMENTS.replace(
                                "{userId}", victim.getId().toString()));

        mockMvc.perform(
                        put(assignmentsPath)
                                .with(csrf())
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, adminAccessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsBytes(
                                                new UpdateUserRolesRequest(Set.of()))))
                .andExpect(status().isOk());

        var reloadedVictim = userRepository.findById(victim.getId()).orElseThrow();
        assertThat(reloadedVictim.getPrivilegeVersion()).isEqualTo(1L);
        assertThat(userRoleRepository.findAllByUser(reloadedVictim)).isEmpty();

        mockMvc.perform(
                        get(SESSIONS_PATH)
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, victimAccessToken)))
                .andExpect(status().isUnauthorized());
    }
}

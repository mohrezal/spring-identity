package com.github.mohrezal.identity.integration.security;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SecurityHttpContractIT extends IntegrationTestSupport {

    private static final String ME_PATH =
            RouteConstants.build(RouteConstants.User.BASE, RouteConstants.User.ME);
    private static final String SESSIONS_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.SESSIONS);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void authenticatedEndpoint_rejectsMissingAccessToken() throws Exception {
        mockMvc.perform(get(ME_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedEndpoint_rejectsAccessTokenForUnknownUser() throws Exception {
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        UUID.randomUUID(), List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ), 0L);

        mockMvc.perform(get(ME_PATH).cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permissionEndpoint_rejectsAuthenticatedUserWithoutRequiredPermission() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE), 0L);

        mockMvc.perform(
                        get(SESSIONS_PATH)
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void permissionEndpoint_acceptsAuthenticatedUserWithRequiredPermission() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ), 0L);

        mockMvc.perform(
                        get(SESSIONS_PATH)
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }
}

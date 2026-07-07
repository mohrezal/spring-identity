package com.github.mohrezal.identity.integration.oauth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.OAUTH_CALLBACK;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.IP_ADDRESS;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.USER_AGENT;
import static com.github.mohrezal.identity.support.oauth.FakeOAuthProvider.CODE;
import static com.github.mohrezal.identity.support.oauth.FakeOAuthProvider.PROVIDER_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.support.OAuthIntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OAuthCallbackEndpointIT extends OAuthIntegrationTestSupport {

    private static final String CALLBACK_PATH =
            RouteConstants.build(RouteConstants.Auth.OAuth.BASE, RouteConstants.Auth.OAuth.CALLBACK)
                    .replace("{provider}", OAuthProviderType.GOOGLE.getName());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private UserOauthConnectionRepository userOauthConnectionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private HashService hashService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void callback_rejectsWrongCorrelationAndConsumesState() throws Exception {
        var state = UUID.randomUUID().toString();
        var correlationId = UUID.randomUUID().toString();
        redisService.set(
                RedisKey.OAUTH_STATE,
                new OAuthStatePayload(
                        OAUTH_CALLBACK,
                        OAuthFlowType.LOGIN,
                        OAuthProviderType.GOOGLE,
                        null,
                        correlationId),
                state);
        var cookieName = applicationProperties.security().cookie().oauthState().name();

        mockMvc.perform(
                        get(CALLBACK_PATH)
                                .queryParam("code", CODE)
                                .queryParam("state", state)
                                .cookie(new Cookie(cookieName, "wrong-correlation")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        assertThat(redisService.get(RedisKey.OAUTH_STATE, OAuthStatePayload.class, state))
                .isEmpty();

        mockMvc.perform(
                        get(CALLBACK_PATH)
                                .queryParam("code", CODE)
                                .queryParam("state", state)
                                .cookie(new Cookie(cookieName, correlationId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void callback_createsOAuthUserAndAuthenticationSession() throws Exception {
        var configuredUserRole = applicationProperties.privilege().role().user();
        roleRepository.saveAndFlush(
                Role.builder()
                        .key(configuredUserRole.key())
                        .name(configuredUserRole.name())
                        .build());
        var state = UUID.randomUUID().toString();
        var correlationId = UUID.randomUUID().toString();
        redisService.set(
                RedisKey.OAUTH_STATE,
                new OAuthStatePayload(
                        OAUTH_CALLBACK,
                        OAuthFlowType.LOGIN,
                        OAuthProviderType.GOOGLE,
                        null,
                        correlationId),
                state);
        var oauthCookieName = applicationProperties.security().cookie().oauthState().name();

        var result =
                mockMvc.perform(
                                get(CALLBACK_PATH)
                                        .queryParam("code", CODE)
                                        .queryParam("state", state)
                                        .cookie(new Cookie(oauthCookieName, correlationId))
                                        .header(HttpHeaders.USER_AGENT, USER_AGENT)
                                        .with(
                                                request -> {
                                                    request.setRemoteAddr(IP_ADDRESS);
                                                    return request;
                                                }))
                        .andExpect(status().isFound())
                        .andExpect(header().string(HttpHeaders.LOCATION, OAUTH_CALLBACK))
                        .andReturn();

        var user = userRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(userCredentialRepository.existsByUser(user)).isFalse();

        var connection =
                userOauthConnectionRepository
                        .findByProviderAndProviderUserId(OAuthProviderType.GOOGLE, PROVIDER_USER_ID)
                        .orElseThrow();
        assertThat(connection.getUser()).isEqualTo(user);
        assertThat(connection.getEmail()).isEqualTo(EMAIL);
        assertThat(userRoleRepository.findAllByUser(user))
                .singleElement()
                .satisfies(
                        assignment ->
                                assertThat(assignment.getRole().getKey())
                                        .isEqualTo(configuredUserRole.key()));

        var accessCookie = result.getResponse().getCookie(CookieConstant.ACCESS_TOKEN);
        var refreshCookie = result.getResponse().getCookie(CookieConstant.REFRESH_TOKEN);
        var clearedOAuthCookie = result.getResponse().getCookie(oauthCookieName);
        assertThat(accessCookie).isNotNull();
        assertThat(jwtTokenProvider.extractUserId(accessCookie.getValue())).contains(user.getId());
        assertThat(refreshCookie).isNotNull();
        assertThat(clearedOAuthCookie).isNotNull();
        assertThat(clearedOAuthCookie.getValue()).isEmpty();
        assertThat(clearedOAuthCookie.getMaxAge()).isZero();

        var session =
                refreshTokenRepository
                        .findByHashedToken(hashService.hashHex(refreshCookie.getValue()))
                        .orElseThrow();
        assertThat(session.getUser()).isEqualTo(user);
        assertThat(session.getIpAddress()).isEqualTo(IP_ADDRESS);
        assertThat(session.getDeviceInfo()).isEqualTo(USER_AGENT);
        assertThat(session.isActive()).isTrue();
    }
}

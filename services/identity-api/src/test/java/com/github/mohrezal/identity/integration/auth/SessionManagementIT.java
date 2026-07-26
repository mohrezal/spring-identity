package com.github.mohrezal.identity.integration.auth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.IP_ADDRESS;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.OTHER_IP_ADDRESS;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.OTHER_USER_AGENT;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.dto.SessionSummary;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Transactional
class SessionManagementIT extends IntegrationTestSupport {

    private static final String SESSIONS_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.SESSIONS);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private HashService hashService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void sessions_identifiesOnlyThePresentedRefreshTokenAsCurrent() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var currentRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        var currentSession =
                refreshTokenRepository.saveAndFlush(
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(currentRawRefreshToken))
                                .deviceInfo(USER_AGENT)
                                .ipAddress(IP_ADDRESS)
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(currentRawRefreshToken)
                                                .orElseThrow())
                                .build());
        var otherRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        var otherSession =
                refreshTokenRepository.saveAndFlush(
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(otherRawRefreshToken))
                                .deviceInfo(OTHER_USER_AGENT)
                                .ipAddress(OTHER_IP_ADDRESS)
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(otherRawRefreshToken)
                                                .orElseThrow())
                                .build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ));

        var result =
                mockMvc.perform(
                                get(SESSIONS_PATH)
                                        .cookie(
                                                new Cookie(
                                                        CookieConstant.ACCESS_TOKEN, accessToken),
                                                new Cookie(
                                                        CookieConstant.REFRESH_TOKEN,
                                                        currentRawRefreshToken)))
                        .andExpect(status().isOk())
                        .andReturn();

        var sessions =
                Arrays.asList(
                        objectMapper.readValue(
                                result.getResponse().getContentAsByteArray(),
                                SessionSummary[].class));
        assertThat(sessions)
                .extracting(SessionSummary::id)
                .containsExactlyInAnyOrder(currentSession.getId(), otherSession.getId());
        assertThat(sessions)
                .filteredOn(SessionSummary::isCurrentSession)
                .singleElement()
                .satisfies(
                        session -> {
                            assertThat(session.id()).isEqualTo(currentSession.getId());
                            assertThat(session.deviceInfo()).isEqualTo(USER_AGENT);
                            assertThat(session.ipAddress()).isEqualTo(IP_ADDRESS);
                        });
    }

    @Test
    void revokeSession_rejectsTheCurrentSession() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var currentRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        var currentSession =
                refreshTokenRepository.saveAndFlush(
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(currentRawRefreshToken))
                                .deviceInfo(USER_AGENT)
                                .ipAddress(IP_ADDRESS)
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(currentRawRefreshToken)
                                                .orElseThrow())
                                .build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE));
        var currentSessionPath =
                RouteConstants.build(SESSIONS_PATH, currentSession.getId().toString());

        mockMvc.perform(
                        delete(currentSessionPath)
                                .with(csrf())
                                .cookie(
                                        new Cookie(CookieConstant.ACCESS_TOKEN, accessToken),
                                        new Cookie(
                                                CookieConstant.REFRESH_TOKEN,
                                                currentRawRefreshToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_CANNOT_REVOKE_CURRENT_SESSION"));

        assertThat(refreshTokenRepository.findById(currentSession.getId()).orElseThrow().isActive())
                .isTrue();
    }

    @Test
    void revokeSession_revokesAnotherSession() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var currentRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        var currentSession =
                refreshTokenRepository.saveAndFlush(
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(currentRawRefreshToken))
                                .deviceInfo(USER_AGENT)
                                .ipAddress(IP_ADDRESS)
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(currentRawRefreshToken)
                                                .orElseThrow())
                                .build());
        var otherRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        var otherSession =
                refreshTokenRepository.saveAndFlush(
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(otherRawRefreshToken))
                                .deviceInfo(OTHER_USER_AGENT)
                                .ipAddress(OTHER_IP_ADDRESS)
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(otherRawRefreshToken)
                                                .orElseThrow())
                                .build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE));
        var otherSessionPath = RouteConstants.build(SESSIONS_PATH, otherSession.getId().toString());

        mockMvc.perform(
                        delete(otherSessionPath)
                                .with(csrf())
                                .cookie(
                                        new Cookie(CookieConstant.ACCESS_TOKEN, accessToken),
                                        new Cookie(
                                                CookieConstant.REFRESH_TOKEN,
                                                currentRawRefreshToken)))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findById(currentSession.getId()).orElseThrow().isActive())
                .isTrue();
        assertThat(refreshTokenRepository.findById(otherSession.getId()).orElseThrow().isRevoked())
                .isTrue();
    }
}

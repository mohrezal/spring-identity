package com.github.mohrezal.identity.integration.auth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class RefreshTokenEndpointIT extends IntegrationTestSupport {

    private static final String REFRESH_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private HashService hashService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void refresh_rotatesTheSession() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var rawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .user(user)
                        .hashedToken(hashService.hashHex(rawRefreshToken))
                        .deviceInfo(USER_AGENT)
                        .expiresAt(
                                jwtTokenProvider.extractExpiration(rawRefreshToken).orElseThrow())
                        .build());

        var result =
                mockMvc.perform(
                                post(REFRESH_PATH)
                                        .with(csrf())
                                        .cookie(
                                                new Cookie(
                                                        CookieConstant.REFRESH_TOKEN,
                                                        rawRefreshToken))
                                        .header(HttpHeaders.USER_AGENT, USER_AGENT))
                        .andExpect(status().isNoContent())
                        .andReturn();

        var rotatedCookie = result.getResponse().getCookie(CookieConstant.REFRESH_TOKEN);
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(rawRefreshToken);
        assertThat(
                        refreshTokenRepository
                                .findByHashedToken(hashService.hashHex(rawRefreshToken))
                                .orElseThrow()
                                .isRevoked())
                .isTrue();
        assertThat(
                        refreshTokenRepository
                                .findByHashedToken(hashService.hashHex(rotatedCookie.getValue()))
                                .orElseThrow()
                                .isActive())
                .isTrue();
        assertThat(refreshTokenRepository.findAll()).hasSize(2);
    }

    @Test
    void refresh_rejectsAReplayedToken() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        var rawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .user(user)
                        .hashedToken(hashService.hashHex(rawRefreshToken))
                        .deviceInfo(USER_AGENT)
                        .expiresAt(
                                jwtTokenProvider.extractExpiration(rawRefreshToken).orElseThrow())
                        .build());
        var refreshCookie = new Cookie(CookieConstant.REFRESH_TOKEN, rawRefreshToken);

        mockMvc.perform(
                        post(REFRESH_PATH)
                                .with(csrf())
                                .cookie(refreshCookie)
                                .header(HttpHeaders.USER_AGENT, USER_AGENT))
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        post(REFRESH_PATH)
                                .with(csrf())
                                .cookie(refreshCookie)
                                .header(HttpHeaders.USER_AGENT, USER_AGENT))
                .andExpect(status().isUnauthorized());
        assertThat(refreshTokenRepository.findAll()).hasSize(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void refresh_allowsOnlyOneConcurrentRotation() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder()
                                .email("concurrent-" + UUID.randomUUID() + "@client.test")
                                .firstName("Test")
                                .lastName("User")
                                .build());
        var rawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .user(user)
                        .hashedToken(hashService.hashHex(rawRefreshToken))
                        .deviceInfo(USER_AGENT)
                        .expiresAt(
                                jwtTokenProvider.extractExpiration(rawRefreshToken).orElseThrow())
                        .build());
        var requestCount = 8;
        var ready = new CountDownLatch(requestCount);
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(requestCount)) {
            var responses = new ArrayList<Future<Integer>>();
            IntStream.range(0, requestCount)
                    .forEach(
                            ignored ->
                                    responses.add(
                                            executor.submit(
                                                    () -> {
                                                        ready.countDown();
                                                        start.await();
                                                        return mockMvc.perform(
                                                                        post(REFRESH_PATH)
                                                                                .with(csrf())
                                                                                .cookie(
                                                                                        new Cookie(
                                                                                                CookieConstant
                                                                                                        .REFRESH_TOKEN,
                                                                                                rawRefreshToken))
                                                                                .header(
                                                                                        HttpHeaders
                                                                                                .USER_AGENT,
                                                                                        USER_AGENT))
                                                                .andReturn()
                                                                .getResponse()
                                                                .getStatus();
                                                    })));

            var allRequestsReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertThat(allRequestsReady).isTrue();

            var statuses = new ArrayList<Integer>();
            for (var response : responses) {
                statuses.add(response.get(10, TimeUnit.SECONDS));
            }

            assertThat(statuses).containsOnlyOnce(HttpStatus.NO_CONTENT.value());
            assertThat(statuses)
                    .filteredOn(status -> status == HttpStatus.UNAUTHORIZED.value())
                    .hasSize(requestCount - 1);
            assertThat(refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user)).hasSize(1);
            assertThat(
                            refreshTokenRepository
                                    .findByHashedToken(hashService.hashHex(rawRefreshToken))
                                    .orElseThrow()
                                    .isRevoked())
                    .isTrue();
        } finally {
            userRepository.deleteById(user.getId());
        }
    }

    @Test
    void refresh_rejectsDisabledAccountWithoutRevokingSession() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder()
                                .email(EMAIL)
                                .firstName("Test")
                                .lastName("User")
                                .enabled(false)
                                .build());
        var rawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .user(user)
                        .hashedToken(hashService.hashHex(rawRefreshToken))
                        .deviceInfo(USER_AGENT)
                        .expiresAt(
                                jwtTokenProvider.extractExpiration(rawRefreshToken).orElseThrow())
                        .build());

        mockMvc.perform(
                        post(REFRESH_PATH)
                                .with(csrf())
                                .cookie(new Cookie(CookieConstant.REFRESH_TOKEN, rawRefreshToken))
                                .header(HttpHeaders.USER_AGENT, USER_AGENT))
                .andExpect(status().isForbidden());

        assertThat(
                        refreshTokenRepository
                                .findByHashedToken(hashService.hashHex(rawRefreshToken))
                                .orElseThrow()
                                .isRevoked())
                .isFalse();
        assertThat(refreshTokenRepository.findAll()).hasSize(1);
    }
}

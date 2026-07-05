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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
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
}

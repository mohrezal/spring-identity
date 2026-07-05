package com.github.mohrezal.identity.integration.auth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Account.NEW_PASSWORD;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.PASSWORD_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.dto.ResetPasswordRequest;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Transactional
class PasswordResetEndpointIT extends IntegrationTestSupport {

    private static final String RESET_PASSWORD_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.RESET_PASSWORD);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private UserOauthConnectionRepository userOauthConnectionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HashService hashService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void resetPassword_createsCredentialForOAuthOnlyUserAndRevokesActiveSessions()
            throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        userOauthConnectionRepository.saveAndFlush(
                UserOauthConnection.builder()
                        .user(user)
                        .provider(OAuthProviderType.GOOGLE)
                        .providerUserId("google-user")
                        .email(EMAIL)
                        .build());
        var firstRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        var secondRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveAllAndFlush(
                List.of(
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(firstRawRefreshToken))
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(firstRawRefreshToken)
                                                .orElseThrow())
                                .build(),
                        RefreshToken.builder()
                                .user(user)
                                .hashedToken(hashService.hashHex(secondRawRefreshToken))
                                .expiresAt(
                                        jwtTokenProvider
                                                .extractExpiration(secondRawRefreshToken)
                                                .orElseThrow())
                                .build()));
        var resetToken = UUID.randomUUID();
        redisService.set(RedisKey.PASSWORD_RESET_TOKEN, EMAIL, resetToken.toString());

        mockMvc.perform(
                        post(RESET_PASSWORD_PATH)
                                .with(csrf())
                                .queryParam("redirectUrl", PASSWORD_RESET)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsBytes(
                                                new ResetPasswordRequest(
                                                        resetToken, NEW_PASSWORD))))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, PASSWORD_RESET));

        var credential = userCredentialRepository.findByUser(user).orElseThrow();
        assertThat(passwordEncoder.matches(NEW_PASSWORD, credential.getHashedPassword())).isTrue();
        assertThat(refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user)).isEmpty();
        assertThat(refreshTokenRepository.findAll())
                .allSatisfy(session -> assertThat(session.isRevoked()).isTrue());
    }

    @Test
    void resetPassword_rejectsReuseOfConsumedToken() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder().email(EMAIL).firstName("Test").lastName("User").build());
        userOauthConnectionRepository.saveAndFlush(
                UserOauthConnection.builder()
                        .user(user)
                        .provider(OAuthProviderType.GOOGLE)
                        .providerUserId("google-user")
                        .email(EMAIL)
                        .build());
        var resetToken = UUID.randomUUID();
        redisService.set(RedisKey.PASSWORD_RESET_TOKEN, EMAIL, resetToken.toString());
        var request = new ResetPasswordRequest(resetToken, NEW_PASSWORD);

        mockMvc.perform(
                        post(RESET_PASSWORD_PATH)
                                .with(csrf())
                                .queryParam("redirectUrl", PASSWORD_RESET)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isFound());

        mockMvc.perform(
                        post(RESET_PASSWORD_PATH)
                                .with(csrf())
                                .queryParam("redirectUrl", PASSWORD_RESET)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_PASSWORD_RESET_TOKEN_NOT_FOUND"));
    }
}

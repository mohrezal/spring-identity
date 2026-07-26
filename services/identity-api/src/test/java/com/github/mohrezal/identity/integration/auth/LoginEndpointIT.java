package com.github.mohrezal.identity.integration.auth;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Account.PASSWORD;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.dto.LoginRequest;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.model.UserCredential;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Transactional
class LoginEndpointIT extends IntegrationTestSupport {

    private static final String LOGIN_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.LOGIN);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HashService hashService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void login_issuesCookiesAndPersistsOnlyTheRefreshTokenHash() throws Exception {
        var user =
                userRepository.saveAndFlush(
                        User.builder()
                                .email(EMAIL)
                                .firstName("Test")
                                .lastName("User")
                                .emailVerifiedAt(OffsetDateTime.now())
                                .build());
        userCredentialRepository.saveAndFlush(
                UserCredential.builder()
                        .user(user)
                        .hashedPassword(passwordEncoder.encode(PASSWORD))
                        .build());

        var result =
                mockMvc.perform(
                                post(LOGIN_PATH)
                                        .with(csrf())
                                        .header(HttpHeaders.USER_AGENT, USER_AGENT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsBytes(
                                                        new LoginRequest(EMAIL, PASSWORD))))
                        .andExpect(status().isOk())
                        .andReturn();

        var accessCookie = result.getResponse().getCookie(CookieConstant.ACCESS_TOKEN);
        var refreshCookie = result.getResponse().getCookie(CookieConstant.REFRESH_TOKEN);
        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.isHttpOnly()).isTrue();
        assertThat(accessCookie.getPath()).isEqualTo("/");
        assertThat(jwtTokenProvider.extractUserId(accessCookie.getValue())).contains(user.getId());
        assertThat(jwtTokenProvider.extractPermissionKeys(accessCookie.getValue())).isEmpty();
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo(RouteConstants.Auth.BASE);
        assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(header -> assertThat(header).contains("SameSite=Lax"));

        var session =
                refreshTokenRepository
                        .findByHashedToken(hashService.hashHex(refreshCookie.getValue()))
                        .orElseThrow();
        assertThat(session.getHashedToken())
                .isNotEqualTo(refreshCookie.getValue())
                .isEqualTo(hashService.hashHex(refreshCookie.getValue()));
        assertThat(session.getDeviceInfo()).isEqualTo(USER_AGENT);
        assertThat(session.isActive()).isTrue();
    }
}

package com.github.mohrezal.identity.integration.security;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Account.PASSWORD;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.OAUTH_CALLBACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.config.ratelimit.RateLimitConfig;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.dto.LoginRequest;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Transactional
class RateLimitHttpIT extends IntegrationTestSupport {

    private static final String LOGIN_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.LOGIN);
    private static final String OAUTH_LINK_PATH =
            RouteConstants.build(
                    RouteConstants.Auth.OAuth.BASE,
                    RouteConstants.Auth.OAuth.LINK.replace(
                            "{provider}", OAuthProviderType.GOOGLE.getName()));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void loginRateLimit_rejectsRequestsAfterConfiguredIpLimit() throws Exception {
        var policy = rateLimitConfig.fromPath(HttpMethod.POST, LOGIN_PATH).orElseThrow();
        assertThat(policy.ipLimit()).isNotNull();
        var clientAddress = UUID.randomUUID().toString();
        var request = new LoginRequest(EMAIL, PASSWORD);

        for (var attempt = 0; attempt < policy.ipLimit(); attempt++) {
            mockMvc.perform(
                            post(LOGIN_PATH)
                                    .with(csrf())
                                    .with(
                                            servletRequest -> {
                                                servletRequest.setRemoteAddr(clientAddress);
                                                return servletRequest;
                                            })
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                        post(LOGIN_PATH)
                                .with(csrf())
                                .with(
                                        servletRequest -> {
                                            servletRequest.setRemoteAddr(clientAddress);
                                            return servletRequest;
                                        })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void oauthLinkRateLimit_rejectsRequestsAfterConfiguredUserLimit() throws Exception {
        var policy = rateLimitConfig.fromPath(HttpMethod.GET, OAUTH_LINK_PATH).orElseThrow();
        assertThat(policy.userLimit()).isNotNull();
        var user =
                userRepository.saveAndFlush(
                        User.builder()
                                .email(UUID.randomUUID() + "-" + EMAIL)
                                .firstName("Test")
                                .lastName("User")
                                .build());
        var accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(), List.of(Permissions.IDENTITY_AUTH_OAUTH_CONNECTIONS_LINK));

        for (var attempt = 0; attempt < policy.userLimit(); attempt++) {
            mockMvc.perform(
                            get(OAUTH_LINK_PATH)
                                    .queryParam("redirect_url", OAUTH_CALLBACK)
                                    .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                    .andExpect(status().isFound());
        }

        mockMvc.perform(
                        get(OAUTH_LINK_PATH)
                                .queryParam("redirect_url", OAUTH_CALLBACK)
                                .cookie(new Cookie(CookieConstant.ACCESS_TOKEN, accessToken)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }
}

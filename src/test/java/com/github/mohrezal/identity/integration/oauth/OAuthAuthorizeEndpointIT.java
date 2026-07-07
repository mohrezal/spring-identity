package com.github.mohrezal.identity.integration.oauth;

import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.OAUTH_CALLBACK;
import static com.github.mohrezal.identity.support.oauth.FakeOAuthProvider.AUTHORIZATION_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.support.OAuthIntegrationTestSupport;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

class OAuthAuthorizeEndpointIT extends OAuthIntegrationTestSupport {

    private static final String AUTHORIZE_PATH =
            RouteConstants.build(
                            RouteConstants.Auth.OAuth.BASE, RouteConstants.Auth.OAuth.AUTHORIZE)
                    .replace("{provider}", OAuthProviderType.GOOGLE.getName());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Test
    void authorize_storesLoginStateAndReturnsProviderRedirect() throws Exception {
        var result =
                mockMvc.perform(get(AUTHORIZE_PATH).queryParam("redirect_url", OAUTH_CALLBACK))
                        .andExpect(status().isFound())
                        .andReturn();

        var location = result.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location).isNotNull().startsWith(AUTHORIZATION_URL);
        var state =
                UriComponentsBuilder.fromUriString(location)
                        .build()
                        .getQueryParams()
                        .getFirst("state");
        assertThat(state).isNotBlank();

        var payload = redisService.get(RedisKey.OAUTH_STATE, OAuthStatePayload.class, state);
        assertThat(payload).isPresent();
        var storedState = payload.orElseThrow();
        assertThat(storedState.redirectUrl()).isEqualTo(OAUTH_CALLBACK);
        assertThat(storedState.flowType()).isEqualTo(OAuthFlowType.LOGIN);
        assertThat(storedState.provider()).isEqualTo(OAuthProviderType.GOOGLE);
        assertThat(storedState.userId()).isNull();

        var cookieName = applicationProperties.security().cookie().oauthState().name();
        var correlationCookie = result.getResponse().getCookie(cookieName);
        assertThat(correlationCookie).isNotNull();
        assertThat(correlationCookie.isHttpOnly()).isTrue();
        assertThat(correlationCookie.getPath()).isEqualTo(RouteConstants.Auth.OAuth.BASE);
        assertThat(correlationCookie.getValue()).isEqualTo(storedState.correlationId());
        assertThat(redisTemplate.getExpire(RedisKey.OAUTH_STATE.resolve(state), TimeUnit.SECONDS))
                .isPositive();
    }
}

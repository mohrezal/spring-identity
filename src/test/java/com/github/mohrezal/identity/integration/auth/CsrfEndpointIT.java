package com.github.mohrezal.identity.integration.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.dto.CsrfTokenResponse;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class CsrfEndpointIT extends IntegrationTestSupport {

    private static final String CSRF_PATH =
            RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.CSRF);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void csrf_returnsTokenHeaderNameAndReadableCookie() throws Exception {
        var result = mockMvc.perform(get(CSRF_PATH)).andExpect(status().isOk()).andReturn();
        var response =
                objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), CsrfTokenResponse.class);

        var cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isFalse();
        assertThat(response.token()).isNotBlank();
        assertThat(response.headerName()).isEqualTo("X-XSRF-TOKEN");
    }
}

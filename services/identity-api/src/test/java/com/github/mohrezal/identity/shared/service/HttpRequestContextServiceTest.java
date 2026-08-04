package com.github.mohrezal.identity.shared.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class HttpRequestContextServiceTest {

    @Test
    void getClientIp_ignoresUntrustedForwardingHeaders() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.10");
        request.addHeader("X-Forwarded-For", "198.51.100.1");
        request.addHeader("Proxy-Client-IP", "198.51.100.2");
        request.addHeader("WL-Proxy-Client-IP", "198.51.100.3");
        request.addHeader("HTTP_X_FORWARDED_FOR", "198.51.100.4");
        request.addHeader("HTTP_CLIENT_IP", "198.51.100.5");
        var service = new HttpRequestContextService(null);

        assertThat(service.getClientIp(request)).isEqualTo("192.0.2.10");
    }
}

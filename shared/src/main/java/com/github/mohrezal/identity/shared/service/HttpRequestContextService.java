package com.github.mohrezal.identity.shared.service;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpRequestContextService {

    private static final String CLIENT_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

    private final Tracer tracer;

    public Optional<String> getTraceId() {
        var currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return Optional.empty();
        }
        return Optional.of(currentSpan.context().traceId());
    }

    public String requireTraceId() {
        return getTraceId()
                .orElseThrow(() -> new IllegalStateException("Active trace ID is required"));
    }

    public Optional<String> getClientRequestId(HttpServletRequest request) {
        return getHeader(request, CLIENT_REQUEST_ID_HEADER);
    }

    public Optional<String> getUserAgent(HttpServletRequest request) {
        return getHeader(request, HttpHeaders.USER_AGENT);
    }

    public Optional<String> getForwardedHost(HttpServletRequest request) {
        return getHeader(request, FORWARDED_HOST_HEADER).map(this::firstForwardedValue);
    }

    public Optional<String> getForwardedProto(HttpServletRequest request) {
        return getHeader(request, FORWARDED_PROTO_HEADER)
                .map(this::firstForwardedValue)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(value -> value.equals("http") || value.equals("https"));
    }

    public String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private Optional<String> getHeader(HttpServletRequest request, String headerName) {
        var value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }

    private String firstForwardedValue(String value) {
        return value.split(",", 2)[0].trim();
    }
}

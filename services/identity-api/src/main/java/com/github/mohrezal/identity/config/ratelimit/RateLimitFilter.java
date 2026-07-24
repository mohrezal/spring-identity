package com.github.mohrezal.identity.config.ratelimit;

import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.HashService;
import com.github.mohrezal.identity.shared.service.HttpRequestContextService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int SUBJECT_HASH_LENGTH = 16;

    private final RateLimitConfig rateLimitConfig;
    private final RedisService redisService;
    private final HttpRequestContextService httpRequestContextProvider;
    private final HashService hashService;

    private record ConsumptionResult(boolean allowed, int remainingTokens, int retryAfterSeconds) {}

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var path = request.getRequestURI();
        var method = HttpMethod.valueOf(request.getMethod());
        var policy = rateLimitConfig.fromPath(method, path).orElse(null);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (policy.ipLimit() != null) {
            var ipSubject =
                    hashService
                            .hashHex(httpRequestContextProvider.getClientIp(request))
                            .substring(0, SUBJECT_HASH_LENGTH);
            var result =
                    tryConsume(
                            RedisKey.RATE_LIMIT_IP,
                            policy.key(),
                            ipSubject,
                            policy.ipLimit(),
                            policy.window());
            if (handleRateLimitExceeded(response, result)) {
                return;
            }
        }

        if (policy.userLimit() != null) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                var result =
                        tryConsume(
                                RedisKey.RATE_LIMIT_USER,
                                policy.key(),
                                user.getId().toString(),
                                policy.userLimit(),
                                policy.window());
                if (handleRateLimitExceeded(response, result)) {
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private ConsumptionResult tryConsume(
            RedisKey redisKey, String policyKey, String subjectId, Integer limit, Duration window) {
        var counterState = redisService.increment(redisKey, window, policyKey, subjectId);
        var allowed = counterState.value() <= limit;
        var remaining = allowed ? Math.max(0, limit - counterState.value()) : 0;

        return new ConsumptionResult(allowed, remaining, counterState.ttlSeconds());
    }

    private boolean handleRateLimitExceeded(
            HttpServletResponse response, ConsumptionResult result) {
        if (result.allowed()) {
            return false;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
        return true;
    }
}

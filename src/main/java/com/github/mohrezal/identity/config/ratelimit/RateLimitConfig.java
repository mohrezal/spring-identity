package com.github.mohrezal.identity.config.ratelimit;

import com.github.mohrezal.identity.config.ApplicationProperties;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class RateLimitConfig {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final ApplicationProperties applicationProperties;

    public Optional<ApplicationProperties.RateLimit.Policy> fromPath(
            HttpMethod method, String path) {
        var rateLimit = applicationProperties.rateLimit();

        for (ApplicationProperties.RateLimit.Policy policy : rateLimit.policies()) {
            if (policy.method().equals(method) && MATCHER.match(policy.path(), path)) {
                return Optional.of(policy);
            }
        }

        return Optional.empty();
    }
}

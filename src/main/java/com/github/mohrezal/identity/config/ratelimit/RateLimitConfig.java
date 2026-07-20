package com.github.mohrezal.identity.config.ratelimit;

import com.github.mohrezal.identity.config.ApplicationProperties;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Component
public class RateLimitConfig {

    private record CompiledPolicy(
            ApplicationProperties.RateLimit.Policy policy, PathPattern pattern) {}

    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    private final List<CompiledPolicy> policies;

    public RateLimitConfig(ApplicationProperties applicationProperties) {
        this.policies =
                applicationProperties.rateLimit().policies().stream()
                        .map(p -> new CompiledPolicy(p, PARSER.parse(p.path())))
                        .toList();
    }

    public Optional<ApplicationProperties.RateLimit.Policy> fromPath(
            HttpMethod method, String path) {

        var pathContainer = PathContainer.parsePath(path);

        for (CompiledPolicy cp : policies) {
            if (cp.policy().method().equals(method) && cp.pattern().matches(pathContainer)) {
                return Optional.of(cp.policy());
            }
        }

        return Optional.empty();
    }
}

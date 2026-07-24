package com.github.mohrezal.identity.shared.enums;

import java.time.Duration;
import lombok.Getter;

public enum RedisKey {
    EMAIL_VERIFICATION_TOKEN("user:email:verification:%s", Duration.ofHours(24L)),
    PASSWORD_RESET_TOKEN("user:password:reset:%s", Duration.ofMinutes(30L)),
    OAUTH_STATE("oauth:state:%s", Duration.ofMinutes(10)),
    RATE_LIMIT_IP("rate-limit:%s:ip:%s", Duration.ZERO),
    RATE_LIMIT_USER("rate-limit:%s:user:%s", Duration.ZERO),
    AUDIT_EVENTS("audit:events", Duration.ZERO);
    private final String pattern;
    @Getter private final Duration ttl;

    RedisKey(String pattern, Duration ttl) {
        this.pattern = pattern;
        this.ttl = ttl;
    }

    public String resolve(String... values) {
        return pattern.formatted((Object[]) values);
    }
}

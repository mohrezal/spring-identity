package com.github.mohrezal.identity.shared.enums;

import java.time.Duration;
import lombok.Getter;

public enum RedisKey {
    EMAIL_VERIFICATION_TOKEN("user:email:verification:%s", Duration.ofHours(24L));

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

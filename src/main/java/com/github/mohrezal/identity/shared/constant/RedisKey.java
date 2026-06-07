package com.github.mohrezal.identity.shared.constant;

public enum RedisKey {
    EMAIL_VERIFICATION_TOKEN("user:email:verification:%s");

    private final String pattern;

    RedisKey(String pattern) {
        this.pattern = pattern;
    }

    public String resolve(String... values) {
        return pattern.formatted((Object[]) values);
    }
}

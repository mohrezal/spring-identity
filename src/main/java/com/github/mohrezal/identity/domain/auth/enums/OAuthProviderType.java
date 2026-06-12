package com.github.mohrezal.identity.domain.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OAuthProviderType {
    GOOGLE("google"),
    TWITTER("twitter");

    private final String name;

    OAuthProviderType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static OAuthProviderType fromName(String name) {
        for (OAuthProviderType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No provider found for: " + name);
    }
}

package com.github.mohrezal.identity.domain.authorization.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Permission {
    IDENTITY_AUTH_SESSIONS_READ("identity.auth.sessions.read"),
    IDENTITY_AUTH_SESSIONS_REVOKE("identity.auth.sessions.revoke"),
    IDENTITY_AUTH_SESSIONS_REVOKE_ALL("identity.auth.sessions.revoke-all"),
    IDENTITY_AUTH_OAUTH_CONNECTIONS_READ("identity.auth.oauth-connections.read"),
    IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK("identity.auth.oauth-connections.unlink"),
    IDENTITY_AUTHORIZATION_ROLES_READ("identity.authorization.roles.read"),
    IDENTITY_AUTHORIZATION_ROLES_CREATE("identity.authorization.roles.create"),
    IDENTITY_AUTHORIZATION_ROLES_UPDATE("identity.authorization.roles.update"),
    IDENTITY_AUTHORIZATION_ROLES_DELETE("identity.authorization.roles.delete"),
    IDENTITY_AUTHORIZATION_ROLES_ASSIGN_PERMISSIONS(
            "identity.authorization.roles.assign-permissions"),
    IDENTITY_AUTHORIZATION_USERS_ASSIGN_ROLES("identity.authorization.users.assign-roles");

    private final String key;

    Permission(String key) {
        this.key = key;
    }

    @JsonValue
    public String key() {
        return key;
    }

    @JsonCreator
    public static Permission fromKey(String key) {
        for (Permission permission : values()) {
            if (permission.key.equalsIgnoreCase(key)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("No permission found for: " + key);
    }
}

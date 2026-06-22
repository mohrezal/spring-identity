package com.github.mohrezal.identity.domain.authorization.constant;

public final class Permissions {

    private Permissions() {}

    public static final String IDENTITY_AUTH_SESSIONS_READ = "identity.auth.sessions.read";
    public static final String IDENTITY_AUTH_SESSIONS_REVOKE = "identity.auth.sessions.revoke";
    public static final String IDENTITY_AUTH_SESSIONS_REVOKE_ALL =
            "identity.auth.sessions.revoke-all";
    public static final String IDENTITY_AUTH_OAUTH_CONNECTIONS_READ =
            "identity.auth.oauth-connections.read";
    public static final String IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK =
            "identity.auth.oauth-connections.unlink";
    public static final String IDENTITY_AUTHORIZATION_ROLES_READ =
            "identity.authorization.roles.read";
    public static final String IDENTITY_AUTHORIZATION_ROLES_CREATE =
            "identity.authorization.roles.create";
    public static final String IDENTITY_AUTHORIZATION_ROLES_UPDATE =
            "identity.authorization.roles.update";
    public static final String IDENTITY_AUTHORIZATION_ROLES_DELETE =
            "identity.authorization.roles.delete";
    public static final String IDENTITY_AUTHORIZATION_ROLES_ASSIGN_PERMISSIONS =
            "identity.authorization.roles.assign-permissions";
    public static final String IDENTITY_AUTHORIZATION_USERS_ASSIGN_ROLES =
            "identity.authorization.users.assign-roles";
}

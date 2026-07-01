package com.github.mohrezal.identity.domain.privilege.constant;

public final class Permissions {

    private Permissions() {}

    public static final String IDENTITY_AUTH_SESSIONS_READ = "identity.auth.sessions.read";
    public static final String IDENTITY_AUTH_SESSIONS_REVOKE = "identity.auth.sessions.revoke";
    public static final String IDENTITY_AUTH_SESSIONS_REVOKE_ALL =
            "identity.auth.sessions.revoke-all";
    public static final String IDENTITY_AUTH_OAUTH_CONNECTIONS_READ =
            "identity.auth.oauth-connections.read";
    public static final String IDENTITY_AUTH_OAUTH_CONNECTIONS_LINK =
            "identity.auth.oauth-connections.link";
    public static final String IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK =
            "identity.auth.oauth-connections.unlink";
    public static final String IDENTITY_PRIVILEGE_PERMISSIONS_READ =
            "identity.privilege.permissions.read";
    public static final String IDENTITY_PRIVILEGE_PERMISSIONS_UPDATE =
            "identity.privilege.permissions.update";
    public static final String IDENTITY_PRIVILEGE_ROLES_READ = "identity.privilege.roles.read";
    public static final String IDENTITY_PRIVILEGE_ROLES_CREATE = "identity.privilege.roles.create";
    public static final String IDENTITY_PRIVILEGE_ROLES_UPDATE = "identity.privilege.roles.update";
    public static final String IDENTITY_PRIVILEGE_ROLES_DELETE = "identity.privilege.roles.delete";
    public static final String IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES =
            "identity.privilege.users.assign-roles";
}

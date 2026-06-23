package com.github.mohrezal.identity.domain.authorization.constant;

import java.util.List;

public final class PermissionCatalog {

    private static final String IDENTITY_SERVICE = "identity";

    private PermissionCatalog() {}

    private static final Definition IDENTITY_AUTH_SESSIONS_READ =
            new Definition(
                    Permissions.IDENTITY_AUTH_SESSIONS_READ,
                    "Read auth sessions",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTH_SESSIONS_REVOKE =
            new Definition(
                    Permissions.IDENTITY_AUTH_SESSIONS_REVOKE,
                    "Revoke auth sessions",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTH_SESSIONS_REVOKE_ALL =
            new Definition(
                    Permissions.IDENTITY_AUTH_SESSIONS_REVOKE_ALL,
                    "Revoke all auth sessions",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTH_OAUTH_CONNECTIONS_READ =
            new Definition(
                    Permissions.IDENTITY_AUTH_OAUTH_CONNECTIONS_READ,
                    "Read OAuth connections",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK =
            new Definition(
                    Permissions.IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK,
                    "Unlink OAuth connections",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTHORIZATION_ROLES_READ =
            new Definition(
                    Permissions.IDENTITY_AUTHORIZATION_ROLES_READ,
                    "Read authorization roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTHORIZATION_ROLES_CREATE =
            new Definition(
                    Permissions.IDENTITY_AUTHORIZATION_ROLES_CREATE,
                    "Create authorization roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTHORIZATION_ROLES_UPDATE =
            new Definition(
                    Permissions.IDENTITY_AUTHORIZATION_ROLES_UPDATE,
                    "Update authorization roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTHORIZATION_ROLES_DELETE =
            new Definition(
                    Permissions.IDENTITY_AUTHORIZATION_ROLES_DELETE,
                    "Delete authorization roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTHORIZATION_ROLES_ASSIGN_PERMISSIONS =
            new Definition(
                    Permissions.IDENTITY_AUTHORIZATION_ROLES_ASSIGN_PERMISSIONS,
                    "Assign permissions to authorization roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_AUTHORIZATION_USERS_ASSIGN_ROLES =
            new Definition(
                    Permissions.IDENTITY_AUTHORIZATION_USERS_ASSIGN_ROLES,
                    "Assign authorization roles to users",
                    IDENTITY_SERVICE);

    public static final List<Definition> USER =
            List.of(
                    IDENTITY_AUTH_SESSIONS_READ,
                    IDENTITY_AUTH_SESSIONS_REVOKE,
                    IDENTITY_AUTH_SESSIONS_REVOKE_ALL,
                    IDENTITY_AUTH_OAUTH_CONNECTIONS_READ,
                    IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK);

    public static final List<Definition> ALL =
            List.of(
                    IDENTITY_AUTH_SESSIONS_READ,
                    IDENTITY_AUTH_SESSIONS_REVOKE,
                    IDENTITY_AUTH_SESSIONS_REVOKE_ALL,
                    IDENTITY_AUTH_OAUTH_CONNECTIONS_READ,
                    IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK,
                    IDENTITY_AUTHORIZATION_ROLES_READ,
                    IDENTITY_AUTHORIZATION_ROLES_CREATE,
                    IDENTITY_AUTHORIZATION_ROLES_UPDATE,
                    IDENTITY_AUTHORIZATION_ROLES_DELETE,
                    IDENTITY_AUTHORIZATION_ROLES_ASSIGN_PERMISSIONS,
                    IDENTITY_AUTHORIZATION_USERS_ASSIGN_ROLES);

    public record Definition(String key, String name, String service) {}
}

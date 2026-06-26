package com.github.mohrezal.identity.domain.privilege.constant;

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
    private static final Definition IDENTITY_PRIVILEGE_PERMISSIONS_READ =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_READ,
                    "Read privilege permissions",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_PRIVILEGE_ROLES_READ =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_ROLES_READ,
                    "Read privilege roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_PRIVILEGE_ROLES_CREATE =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_ROLES_CREATE,
                    "Create privilege roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_PRIVILEGE_ROLES_UPDATE =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_ROLES_UPDATE,
                    "Update privilege roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_PRIVILEGE_ROLES_DELETE =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_ROLES_DELETE,
                    "Delete privilege roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_PRIVILEGE_ROLES_ASSIGN_PERMISSIONS =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_ROLES_ASSIGN_PERMISSIONS,
                    "Assign permissions to privilege roles",
                    IDENTITY_SERVICE);
    private static final Definition IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES =
            new Definition(
                    Permissions.IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES,
                    "Assign privilege roles to users",
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
                    IDENTITY_PRIVILEGE_PERMISSIONS_READ,
                    IDENTITY_PRIVILEGE_ROLES_READ,
                    IDENTITY_PRIVILEGE_ROLES_CREATE,
                    IDENTITY_PRIVILEGE_ROLES_UPDATE,
                    IDENTITY_PRIVILEGE_ROLES_DELETE,
                    IDENTITY_PRIVILEGE_ROLES_ASSIGN_PERMISSIONS,
                    IDENTITY_PRIVILEGE_USERS_ASSIGN_ROLES);

    public record Definition(String key, String name, String service) {}
}

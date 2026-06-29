package com.github.mohrezal.identity.shared.enums;

public enum AppMessage {
    BAD_REQUEST("shared.error.bad-request"),
    UNAUTHORIZED("shared.error.unauthorized"),
    FORBIDDEN("shared.error.forbidden"),
    CONFLICT("shared.error.conflict"),
    NOT_FOUND("shared.error.not-found"),
    METHOD_NOT_ALLOWED("shared.error.method-not-allowed"),
    INTERNAL("shared.error.internal"),
    UNEXPECTED("shared.error.unexpected"),
    INVALID_REDIRECT_URL("shared.error.invalid-redirect-url"),
    VALIDATION_FAILED("shared.validation.failed"),
    AUTH_INVALID_CREDENTIALS("auth.error.invalid-credentials"),
    AUTH_CURRENT_PASSWORD_MISMATCH("auth.error.current-password-mismatch"),
    AUTH_INVALID_REFRESH_TOKEN("auth.error.invalid-refresh-token"),
    AUTH_REFRESH_TOKEN_NOT_FOUND("auth.error.refresh-token-not-found"),
    AUTH_SESSION_NOT_FOUND("auth.error.session-not-found"),
    AUTH_CANNOT_REVOKE_CURRENT_SESSION("auth.error.cannot-revoke-current-session"),
    AUTH_EMAIL_NOT_VERIFIED("auth.error.email-not-verified"),
    AUTH_EMAIL_VERIFICATION_NOT_FOUND("auth.error.email-verification-token-not-found"),
    AUTH_PASSWORD_RESET_TOKEN_NOT_FOUND("auth.error.password-reset-token-not-found"),
    AUTH_EMAIL_ALREADY_VERIFIED("auth.error.email-already-verified"),
    OAUTH_EMAIL_CONFLICT("oauth.error.email-conflict"),
    OAUTH_EMAIL_MISMATCH("oauth.error.email-mismatch"),
    OAUTH_PROVIDER_ALREADY_LINKED("oauth.error.provider-already-linked"),
    OAUTH_CONNECTION_NOT_FOUND("oauth.error.connection-not-found"),
    OAUTH_CANNOT_UNLINK_LAST_LOGIN_METHOD("oauth.error.cannot-unlink-last-login-method"),
    AUTH_REGISTERED("auth.success.registered"),
    USER_EMAIL_ALREADY_EXISTS("user.error.email-already-exists"),
    USER_INVALID_VERIFICATION_TOKEN("user.error.invalid-verification-token"),
    USER_ALREADY_VERIFIED("user.error.already-verified"),
    USER_NOT_FOUND("user.error.not-found"),
    PRIVILEGE_PERMISSION_NOT_FOUND("privilege.error.permission-not-found"),
    PRIVILEGE_ROLE_NOT_FOUND("privilege.error.role-not-found"),
    PRIVILEGE_ROLE_KEY_ALREADY_EXISTS("privilege.error.role-key-already-exists"),
    PRIVILEGE_CONFIGURED_ROLE_CANNOT_BE_DELETED(
            "privilege.error.configured-role-cannot-be-deleted"),
    PRIVILEGE_OWNER_ROLE_CANNOT_BE_UPDATED("privilege.error.owner-role-cannot-be-updated"),
    PRIVILEGE_ROLE_ASSIGNED_TO_USERS("privilege.error.role-assigned-to-users");

    private final String messageKey;

    AppMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}

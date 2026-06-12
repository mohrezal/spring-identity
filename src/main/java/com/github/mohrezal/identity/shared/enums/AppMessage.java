package com.github.mohrezal.identity.shared.enums;

public enum AppMessage {
    BAD_REQUEST("shared.error.bad-request"),
    UNAUTHORIZED("shared.error.unauthorized"),
    FORBIDDEN("shared.error.forbidden"),
    CONFLICT("shared.error.conflict"),
    NOT_FOUND("shared.error.not-found"),
    INTERNAL("shared.error.internal"),
    UNEXPECTED("shared.error.unexpected"),
    INVALID_REDIRECT_URL("shared.error.invalid-redirect-url"),
    VALIDATION_FAILED("shared.validation.failed"),
    AUTH_INVALID_CREDENTIALS("auth.error.invalid-credentials"),
    AUTH_INVALID_REFRESH_TOKEN("auth.error.invalid-refresh-token"),
    AUTH_REFRESH_TOKEN_NOT_FOUND("auth.error.refresh-token-not-found"),
    AUTH_EMAIL_VERIFICATION_NOT_FOUND("auth.error.email-verification-token-not-found"),
    AUTH_EMAIL_ALREADY_VERIFIED("auth.error.email-already-verified"),
    OAUTH_EMAIL_CONFLICT("oauth.error.email-conflict"),
    OAUTH_PROVIDER_ALREADY_LINKED("oauth.error.provider-already-linked"),
    AUTH_REGISTERED("auth.success.registered"),
    USER_EMAIL_ALREADY_EXISTS("user.error.email-already-exists"),
    USER_INVALID_VERIFICATION_TOKEN("user.error.invalid-verification-token"),
    USER_ALREADY_VERIFIED("user.error.already-verified"),
    USER_NOT_FOUND("user.error.not-found");

    private final String messageKey;

    AppMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}

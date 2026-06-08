package com.github.mohrezal.identity.shared.constant;

public final class MessageKey {

    public static final String SHARED_ERROR_BAD_REQUEST = "shared.error.bad-request";
    public static final String SHARED_ERROR_UNAUTHORIZED = "shared.error.unauthorized";
    public static final String SHARED_ERROR_FORBIDDEN = "shared.error.forbidden";
    public static final String SHARED_ERROR_CONFLICT = "shared.error.conflict";
    public static final String SHARED_ERROR_NOT_FOUND = "shared.error.not-found";
    public static final String SHARED_ERROR_INTERNAL = "shared.error.internal";
    public static final String SHARED_ERROR_UNEXPECTED = "shared.error.unexpected";
    public static final String SHARED_ERROR_INVALID_REDIRECT_URL =
            "shared.error.invalid-redirect-url";
    public static final String SHARED_VALIDATION_FAILED = "shared.validation.failed";
    public static final String AUTH_INVALID_CREDENTIALS = "auth.error.invalid-credentials";
    public static final String AUTH_INVALID_REFRESH_TOKEN = "auth.error.invalid-refresh-token";
    public static final String AUTH_REFRESH_TOKEN_NOT_FOUND = "auth.error.refresh-token-not-found";
    public static final String USER_EMAIL_ALREADY_EXISTS = "user.error.email-already-exists";
    public static final String USER_INVALID_VERIFICATION_TOKEN =
            "user.error.invalid-verification-token";
    public static final String USER_ALREADY_VERIFIED = "user.error.already-verified";
    public static final String USER_NOT_FOUND = "user.error.not-found";

    private MessageKey() {}
}

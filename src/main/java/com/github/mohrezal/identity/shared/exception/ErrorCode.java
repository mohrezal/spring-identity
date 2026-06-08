package com.github.mohrezal.identity.shared.exception;

import com.github.mohrezal.identity.shared.constant.MessageKey;

public enum ErrorCode {
    BAD_REQUEST(MessageKey.SHARED_ERROR_BAD_REQUEST),
    UNAUTHORIZED(MessageKey.SHARED_ERROR_UNAUTHORIZED),
    FORBIDDEN(MessageKey.SHARED_ERROR_FORBIDDEN),
    CONFLICT(MessageKey.SHARED_ERROR_CONFLICT),
    NOT_FOUND(MessageKey.SHARED_ERROR_NOT_FOUND),
    INTERNAL(MessageKey.SHARED_ERROR_INTERNAL),
    UNEXPECTED(MessageKey.SHARED_ERROR_UNEXPECTED),
    INVALID_REDIRECT_URL(MessageKey.SHARED_ERROR_INVALID_REDIRECT_URL),
    VALIDATION_FAILED(MessageKey.SHARED_VALIDATION_FAILED),
    AUTH_INVALID_CREDENTIALS(MessageKey.AUTH_INVALID_CREDENTIALS),
    AUTH_INVALID_REFRESH_TOKEN(MessageKey.AUTH_INVALID_REFRESH_TOKEN),
    AUTH_REFRESH_TOKEN_NOT_FOUND(MessageKey.AUTH_REFRESH_TOKEN_NOT_FOUND),
    USER_EMAIL_ALREADY_EXISTS(MessageKey.USER_EMAIL_ALREADY_EXISTS),
    USER_INVALID_VERIFICATION_TOKEN(MessageKey.USER_INVALID_VERIFICATION_TOKEN),
    USER_ALREADY_VERIFIED(MessageKey.USER_ALREADY_VERIFIED),
    USER_NOT_FOUND(MessageKey.USER_NOT_FOUND);

    private final String messageKey;

    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}

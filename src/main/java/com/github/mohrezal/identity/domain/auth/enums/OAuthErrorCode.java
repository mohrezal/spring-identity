package com.github.mohrezal.identity.domain.auth.enums;

import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthProviderAlreadyLinkedException;
import com.github.mohrezal.identity.domain.user.exception.type.UserEmailAlreadyExistsException;
import com.github.mohrezal.identity.shared.exception.type.BaseException;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public enum OAuthErrorCode {
    INVALID_STATE("invalid_state"),
    INVALID_CALLBACK("invalid_callback"),
    EMAIL_CONFLICT("email_conflict"),
    PROVIDER_ALREADY_LINKED("provider_already_linked"),
    PROVIDER_NOT_SUPPORTED("provider_not_supported"),
    CALLBACK_FAILED("callback_failed");

    private final String value;

    OAuthErrorCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OAuthErrorCode from(Exception exception) {
        if (exception instanceof OAuthProviderAlreadyLinkedException) {
            return PROVIDER_ALREADY_LINKED;
        }

        if (exception instanceof OAuthEmailConflictException
                || exception instanceof UserEmailAlreadyExistsException) {
            return EMAIL_CONFLICT;
        }

        if (exception instanceof NotFoundException) {
            return PROVIDER_NOT_SUPPORTED;
        }

        if (exception instanceof UnauthorizedException) {
            return INVALID_CALLBACK;
        }

        if (exception instanceof BaseException) {
            return CALLBACK_FAILED;
        }

        return CALLBACK_FAILED;
    }
}

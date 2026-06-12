package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.domain.auth.enums.OAuthErrorCode;

public class OAuthCallbackRedirectException extends RuntimeException {
    private final String redirectUrl;
    private final OAuthErrorCode errorCode;

    public OAuthCallbackRedirectException(
            String redirectUrl, OAuthErrorCode errorCode, Throwable cause) {
        super(errorCode.value(), cause);
        this.redirectUrl = redirectUrl;
        this.errorCode = errorCode;
    }

    public String redirectUrl() {
        return redirectUrl;
    }

    public OAuthErrorCode errorCode() {
        return errorCode;
    }
}

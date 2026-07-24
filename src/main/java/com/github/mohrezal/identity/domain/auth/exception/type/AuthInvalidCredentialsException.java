package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.domain.auth.exception.context.LoginAuditExceptionContext;
import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthInvalidCredentialsException extends UnauthorizedException {

    public AuthInvalidCredentialsException() {
        super(ExceptionCode.AUTH_INVALID_CREDENTIALS);
    }

    public AuthInvalidCredentialsException(LoginAuditExceptionContext context) {
        super(ExceptionCode.AUTH_INVALID_CREDENTIALS, context);
    }
}

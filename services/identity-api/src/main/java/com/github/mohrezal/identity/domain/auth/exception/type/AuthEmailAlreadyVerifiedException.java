package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthEmailAlreadyVerifiedException extends UnauthorizedException {
    public AuthEmailAlreadyVerifiedException() {
        super(ExceptionCode.AUTH_EMAIL_ALREADY_VERIFIED);
    }

    public AuthEmailAlreadyVerifiedException(ExceptionContext context) {
        super(ExceptionCode.AUTH_EMAIL_ALREADY_VERIFIED, context);
    }
}

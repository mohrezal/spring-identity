package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.domain.auth.exception.context.LoginAuditExceptionContext;
import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ForbiddenException;

public class AuthEmailNotVerifiedException extends ForbiddenException {

    public AuthEmailNotVerifiedException() {
        super(ExceptionCode.AUTH_EMAIL_NOT_VERIFIED);
    }

    public AuthEmailNotVerifiedException(LoginAuditExceptionContext context) {
        super(ExceptionCode.AUTH_EMAIL_NOT_VERIFIED, context);
    }
}

package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.domain.auth.exception.context.LoginAuditExceptionContext;
import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ForbiddenException;

public class AuthEmailNotVerifiedException extends ForbiddenException {

    public AuthEmailNotVerifiedException() {
        super(AppMessage.AUTH_EMAIL_NOT_VERIFIED);
    }

    public AuthEmailNotVerifiedException(LoginAuditExceptionContext context) {
        super(AppMessage.AUTH_EMAIL_NOT_VERIFIED, context);
    }
}

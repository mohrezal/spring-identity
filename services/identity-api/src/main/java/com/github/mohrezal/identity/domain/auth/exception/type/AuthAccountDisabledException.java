package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.domain.auth.exception.context.LoginAuditExceptionContext;
import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ForbiddenException;

public class AuthAccountDisabledException extends ForbiddenException {

    public AuthAccountDisabledException() {
        super(ExceptionCode.AUTH_ACCOUNT_DISABLED);
    }

    public AuthAccountDisabledException(LoginAuditExceptionContext context) {
        super(ExceptionCode.AUTH_ACCOUNT_DISABLED, context);
    }
}

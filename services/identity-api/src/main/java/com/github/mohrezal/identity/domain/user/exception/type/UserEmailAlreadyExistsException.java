package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class UserEmailAlreadyExistsException extends ConflictException {

    public UserEmailAlreadyExistsException() {
        super(ExceptionCode.USER_EMAIL_ALREADY_EXISTS);
    }

    public UserEmailAlreadyExistsException(ExceptionContext context) {
        super(ExceptionCode.USER_EMAIL_ALREADY_EXISTS, context);
    }

    public UserEmailAlreadyExistsException(ExceptionContext context, Throwable cause) {
        super(ExceptionCode.USER_EMAIL_ALREADY_EXISTS, context, cause);
    }
}

package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(ExceptionCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(ExceptionCode appMessage) {
        super(appMessage, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(ExceptionCode appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.FORBIDDEN, context);
    }
}

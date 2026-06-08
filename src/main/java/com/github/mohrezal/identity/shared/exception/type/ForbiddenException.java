package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(ErrorCode errorCode, ExceptionContext context) {
        super(errorCode, HttpStatus.FORBIDDEN, context);
    }
}

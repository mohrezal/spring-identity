package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(ExceptionCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(ExceptionCode appMessage) {
        super(appMessage, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(ExceptionCode appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.UNAUTHORIZED, context);
    }
}

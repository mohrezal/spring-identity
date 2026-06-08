package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(AppMessage.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(AppMessage appMessage) {
        super(appMessage, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(AppMessage appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.UNAUTHORIZED, context);
    }
}

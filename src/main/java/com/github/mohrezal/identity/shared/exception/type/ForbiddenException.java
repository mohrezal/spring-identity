package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(AppMessage.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(AppMessage appMessage) {
        super(appMessage, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(AppMessage appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.FORBIDDEN, context);
    }
}

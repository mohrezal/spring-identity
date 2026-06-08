package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class InternalException extends BaseException {

    public InternalException() {
        super(AppMessage.INTERNAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(AppMessage appMessage) {
        super(appMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(AppMessage appMessage, ExceptionContext context, Throwable cause) {
        super(appMessage, HttpStatus.INTERNAL_SERVER_ERROR, context, cause);
    }
}

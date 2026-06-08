package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {

    public ConflictException() {
        super(AppMessage.CONFLICT, HttpStatus.CONFLICT);
    }

    public ConflictException(AppMessage appMessage) {
        super(appMessage, HttpStatus.CONFLICT);
    }

    public ConflictException(AppMessage appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.CONFLICT, context);
    }
}

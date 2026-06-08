package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(AppMessage.BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(AppMessage appMessage) {
        super(appMessage, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(AppMessage appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.BAD_REQUEST, context);
    }
}

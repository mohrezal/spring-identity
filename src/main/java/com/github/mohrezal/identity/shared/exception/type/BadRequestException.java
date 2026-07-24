package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(ExceptionCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(ExceptionCode appMessage) {
        super(appMessage, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(ExceptionCode appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.BAD_REQUEST, context);
    }
}

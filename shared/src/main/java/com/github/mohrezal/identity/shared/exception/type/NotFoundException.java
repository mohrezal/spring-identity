package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {

    public NotFoundException() {
        super(ExceptionCode.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(ExceptionCode appMessage) {
        super(appMessage, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(ExceptionCode appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.NOT_FOUND, context);
    }
}

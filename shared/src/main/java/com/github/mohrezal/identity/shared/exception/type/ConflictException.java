package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {

    public ConflictException() {
        super(ExceptionCode.CONFLICT, HttpStatus.CONFLICT);
    }

    public ConflictException(ExceptionCode appMessage) {
        super(appMessage, HttpStatus.CONFLICT);
    }

    public ConflictException(ExceptionCode appMessage, ExceptionContext context) {
        super(appMessage, HttpStatus.CONFLICT, context);
    }

    public ConflictException(ExceptionCode appMessage, ExceptionContext context, Throwable cause) {
        super(appMessage, HttpStatus.CONFLICT, context, cause);
    }
}

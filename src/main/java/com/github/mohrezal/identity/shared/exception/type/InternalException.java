package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class InternalException extends BaseException {

    public InternalException() {
        super(ExceptionCode.INTERNAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(ExceptionCode appMessage) {
        super(appMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(ExceptionCode appMessage, ExceptionContext context, Throwable cause) {
        super(appMessage, HttpStatus.INTERNAL_SERVER_ERROR, context, cause);
    }
}

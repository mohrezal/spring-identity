package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class InternalException extends BaseException {

    public InternalException() {
        super(ErrorCode.INTERNAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(ErrorCode errorCode, ExceptionContext context, Throwable cause) {
        super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, context, cause);
    }
}

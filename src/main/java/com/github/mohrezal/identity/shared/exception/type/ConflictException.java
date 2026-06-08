package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {

    public ConflictException() {
        super(ErrorCode.CONFLICT, HttpStatus.CONFLICT);
    }

    public ConflictException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.CONFLICT);
    }

    public ConflictException(ErrorCode errorCode, ExceptionContext context) {
        super(errorCode, HttpStatus.CONFLICT, context);
    }
}

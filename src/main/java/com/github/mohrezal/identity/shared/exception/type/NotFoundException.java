package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {

    public NotFoundException() {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(ErrorCode errorCode, ExceptionContext context) {
        super(errorCode, HttpStatus.NOT_FOUND, context);
    }
}

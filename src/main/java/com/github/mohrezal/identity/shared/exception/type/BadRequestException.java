package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(ErrorCode errorCode, ExceptionContext context) {
        super(errorCode, HttpStatus.BAD_REQUEST, context);
    }
}

package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import com.github.mohrezal.identity.shared.exception.context.NoExceptionContext;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    private final HttpStatus statusCode;

    private final ExceptionContext context;

    protected BaseException(ErrorCode errorCode, HttpStatus statusCode) {
        super(errorCode.messageKey());
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.context = NoExceptionContext.INSTANCE;
    }

    protected BaseException(ErrorCode errorCode, HttpStatus statusCode, ExceptionContext context) {
        super(errorCode.messageKey());
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.context = context;
    }

    protected BaseException(
            ErrorCode errorCode, HttpStatus statusCode, ExceptionContext context, Throwable cause) {
        super(errorCode.messageKey(), cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.context = context;
    }
}

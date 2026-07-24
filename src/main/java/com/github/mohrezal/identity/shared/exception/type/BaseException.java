package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import com.github.mohrezal.identity.shared.exception.context.NoExceptionContext;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    private final HttpStatus statusCode;

    private final ExceptionContext context;

    protected BaseException(ExceptionCode exceptionCode, HttpStatus statusCode) {
        super(exceptionCode.messageKey());
        this.exceptionCode = exceptionCode;
        this.statusCode = statusCode;
        this.context = NoExceptionContext.INSTANCE;
    }

    protected BaseException(
            ExceptionCode exceptionCode, HttpStatus statusCode, ExceptionContext context) {
        super(exceptionCode.messageKey());
        this.exceptionCode = exceptionCode;
        this.statusCode = statusCode;
        this.context = context;
    }

    protected BaseException(
            ExceptionCode exceptionCode,
            HttpStatus statusCode,
            ExceptionContext context,
            Throwable cause) {
        super(exceptionCode.messageKey(), cause);
        this.exceptionCode = exceptionCode;
        this.statusCode = statusCode;
        this.context = context;
    }
}

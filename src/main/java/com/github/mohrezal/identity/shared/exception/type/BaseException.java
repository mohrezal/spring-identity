package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;
import com.github.mohrezal.identity.shared.exception.context.NoExceptionContext;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    private final AppMessage appMessage;

    private final HttpStatus statusCode;

    private final ExceptionContext context;

    protected BaseException(AppMessage appMessage, HttpStatus statusCode) {
        super(appMessage.messageKey());
        this.appMessage = appMessage;
        this.statusCode = statusCode;
        this.context = NoExceptionContext.INSTANCE;
    }

    protected BaseException(
            AppMessage appMessage, HttpStatus statusCode, ExceptionContext context) {
        super(appMessage.messageKey());
        this.appMessage = appMessage;
        this.statusCode = statusCode;
        this.context = context;
    }

    protected BaseException(
            AppMessage appMessage,
            HttpStatus statusCode,
            ExceptionContext context,
            Throwable cause) {
        super(appMessage.messageKey(), cause);
        this.appMessage = appMessage;
        this.statusCode = statusCode;
        this.context = context;
    }
}

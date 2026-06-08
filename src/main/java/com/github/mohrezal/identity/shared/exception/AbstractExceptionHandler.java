package com.github.mohrezal.identity.shared.exception;

import com.github.mohrezal.identity.shared.exception.type.BaseException;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
public abstract class AbstractExceptionHandler {

    protected final MessageSource messageSource;

    protected AbstractExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    protected ResponseEntity<ErrorResponse> buildErrorResponse(
            BaseException exception, WebRequest request) {
        if (exception.getStatusCode().is5xxServerError()) {
            log.error(
                    "Handled exception - code={} status={} context={}",
                    exception.getErrorCode(),
                    exception.getStatusCode().value(),
                    exception.getContext(),
                    exception);
        } else {
            log.warn(
                    "Handled exception - code={} status={} context={}",
                    exception.getErrorCode(),
                    exception.getStatusCode().value(),
                    exception.getContext());
        }

        return ResponseEntity.status(exception.getStatusCode())
                .body(buildBody(exception.getErrorCode(), null, request));
    }

    protected ErrorResponse buildBody(
            ErrorCode errorCode, Map<String, String> errors, WebRequest request) {
        return new ErrorResponse(
                errorCode.name(),
                resolveMessage(errorCode.messageKey()),
                errors,
                Instant.now(),
                resolvePath(request));
    }

    protected String resolveMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private static String resolvePath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }

        return null;
    }
}

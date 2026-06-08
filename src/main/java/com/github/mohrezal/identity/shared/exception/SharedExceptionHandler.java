package com.github.mohrezal.identity.shared.exception;

import com.github.mohrezal.identity.shared.exception.type.BadRequestException;
import com.github.mohrezal.identity.shared.exception.type.BaseException;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;
import com.github.mohrezal.identity.shared.exception.type.ForbiddenException;
import com.github.mohrezal.identity.shared.exception.type.InternalException;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class SharedExceptionHandler extends AbstractExceptionHandler {

    public SharedExceptionHandler(MessageSource messageSource) {
        super(messageSource);
    }

    @ExceptionHandler({
        BadRequestException.class,
        InvalidRedirectUrlException.class,
        ConflictException.class,
        ForbiddenException.class,
        InternalException.class,
        NotFoundException.class,
        UnauthorizedException.class
    })
    public ResponseEntity<ErrorResponse> handleSharedException(
            BaseException exception, WebRequest request) {
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildBody(ErrorCode.AUTH_INVALID_CREDENTIALS, null, request));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBody(ErrorCode.FORBIDDEN, null, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception, WebRequest request) {
        var errors = new HashMap<String, String>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(exception.getStatusCode())
                .body(buildBody(ErrorCode.VALIDATION_FAILED, errors, request));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception, WebRequest request) {
        var errors = new HashMap<String, String>();

        exception
                .getParameterValidationResults()
                .forEach(
                        result -> {
                            var parameterName = result.getMethodParameter().getParameterName();
                            result.getResolvableErrors()
                                    .forEach(
                                            error ->
                                                    errors.put(
                                                            parameterName,
                                                            error.getDefaultMessage()));
                        });

        return ResponseEntity.status(exception.getStatusCode())
                .body(buildBody(ErrorCode.VALIDATION_FAILED, errors, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(ErrorCode.UNEXPECTED, null, request));
    }
}

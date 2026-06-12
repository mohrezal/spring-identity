package com.github.mohrezal.identity.domain.auth.exception;

import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailAlreadyVerifiedException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidCredentialsException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidRefreshTokenException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthRefreshTokenNotFoundException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthProviderAlreadyLinkedException;
import com.github.mohrezal.identity.shared.exception.AbstractExceptionHandler;
import com.github.mohrezal.identity.shared.exception.ErrorResponse;
import com.github.mohrezal.identity.shared.exception.type.BaseException;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler extends AbstractExceptionHandler {

    public AuthExceptionHandler(MessageSource messageSource) {
        super(messageSource);
    }

    @ExceptionHandler({
        AuthInvalidCredentialsException.class,
        AuthInvalidRefreshTokenException.class,
        AuthRefreshTokenNotFoundException.class,
        AuthEmailAlreadyVerifiedException.class,
        OAuthEmailConflictException.class,
        OAuthProviderAlreadyLinkedException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthException(
            BaseException exception, WebRequest request) {
        return buildErrorResponse(exception, request);
    }
}

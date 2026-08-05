package com.github.mohrezal.identity.domain.auth.exception;

import com.github.mohrezal.identity.audit.service.AuditEventFactory;
import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.auth.exception.context.EmailVerificationAuditExceptionContext;
import com.github.mohrezal.identity.domain.auth.exception.context.LoginAuditExceptionContext;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthAccountDisabledException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthCannotRevokeCurrentSessionException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthCurrentPasswordMismatchException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailAlreadyVerifiedException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailNotVerifiedException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidCredentialsException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidRefreshTokenException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthPasswordResetTokenNotFoundException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthRefreshTokenNotFoundException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthSessionNotFoundException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthCannotUnlinkLastLoginMethodException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthConnectionNotFoundException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailMismatchException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthProviderAlreadyLinkedException;
import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.AbstractExceptionHandler;
import com.github.mohrezal.identity.shared.exception.ErrorResponse;
import com.github.mohrezal.identity.shared.exception.type.BaseException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler extends AbstractExceptionHandler {

    private final AuditEventFactory auditEventFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AuthExceptionHandler(
            MessageSource messageSource,
            AuditEventFactory auditEventFactory,
            ApplicationEventPublisher applicationEventPublisher) {
        super(messageSource);
        this.auditEventFactory = auditEventFactory;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @ExceptionHandler(AuthInvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            AuthInvalidCredentialsException exception, WebRequest request) {
        if (exception.getContext()
                instanceof
                LoginAuditExceptionContext(
                        AuditRequestContext auditRequestContext,
                        String attemptedEmail)) {
            applicationEventPublisher.publishEvent(
                    auditEventFactory.loginFailed(
                            auditRequestContext, attemptedEmail, exception.getExceptionCode()));
        }
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(AuthEmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(
            AuthEmailNotVerifiedException exception, WebRequest request) {
        if (exception.getContext()
                instanceof
                LoginAuditExceptionContext(
                        AuditRequestContext auditRequestContext,
                        String attemptedEmail)) {
            applicationEventPublisher.publishEvent(
                    auditEventFactory.loginFailed(
                            auditRequestContext, attemptedEmail, exception.getExceptionCode()));
        }
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(AuthAccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(
            AuthAccountDisabledException exception, WebRequest request) {
        if (exception.getContext()
                instanceof
                LoginAuditExceptionContext(
                        AuditRequestContext auditRequestContext,
                        String attemptedEmail)) {
            applicationEventPublisher.publishEvent(
                    auditEventFactory.loginFailed(
                            auditRequestContext, attemptedEmail, exception.getExceptionCode()));
        }
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(AuthEmailAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyVerified(
            AuthEmailAlreadyVerifiedException exception, WebRequest request) {
        if (exception.getContext()
                instanceof
                EmailVerificationAuditExceptionContext(
                        AuditRequestContext auditRequestContext,
                        String email)) {
            applicationEventPublisher.publishEvent(
                    auditEventFactory.emailVerificationFailed(
                            auditRequestContext, email, exception.getExceptionCode()));
        }
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler({
        AuthCurrentPasswordMismatchException.class,
        AuthInvalidRefreshTokenException.class,
        AuthPasswordResetTokenNotFoundException.class,
        AuthRefreshTokenNotFoundException.class,
        AuthSessionNotFoundException.class,
        AuthCannotRevokeCurrentSessionException.class,
        OAuthConnectionNotFoundException.class,
        OAuthCannotUnlinkLastLoginMethodException.class,
        OAuthEmailConflictException.class,
        OAuthEmailMismatchException.class,
        OAuthProviderAlreadyLinkedException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthException(
            BaseException exception, WebRequest request) {
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildBody(ExceptionCode.AUTH_INVALID_CREDENTIALS, null, request));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBody(ExceptionCode.FORBIDDEN, null, request));
    }
}

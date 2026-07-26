package com.github.mohrezal.identity.domain.user.exception;

import com.github.mohrezal.identity.audit.service.AuditEventFactory;
import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.user.exception.context.RegistrationAuditExceptionContext;
import com.github.mohrezal.identity.domain.user.exception.type.UserAlreadyVerifiedException;
import com.github.mohrezal.identity.domain.user.exception.type.UserEmailAlreadyExistsException;
import com.github.mohrezal.identity.domain.user.exception.type.UserInvalidVerificationTokenException;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.shared.exception.AbstractExceptionHandler;
import com.github.mohrezal.identity.shared.exception.ErrorResponse;
import com.github.mohrezal.identity.shared.exception.type.BaseException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserExceptionHandler extends AbstractExceptionHandler {

    private final AuditEventFactory auditEventFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UserExceptionHandler(
            MessageSource messageSource,
            AuditEventFactory auditEventFactory,
            ApplicationEventPublisher applicationEventPublisher) {
        super(messageSource);
        this.auditEventFactory = auditEventFactory;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @ExceptionHandler(UserEmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            UserEmailAlreadyExistsException exception, WebRequest request) {
        if (exception.getContext()
                instanceof
                RegistrationAuditExceptionContext(
                        AuditRequestContext auditRequestContext,
                        String attemptedEmail)) {
            applicationEventPublisher.publishEvent(
                    auditEventFactory.registerFailed(
                            auditRequestContext, attemptedEmail, exception.getExceptionCode()));
        }
        return buildErrorResponse(exception, request);
    }

    @ExceptionHandler({
        UserAlreadyVerifiedException.class,
        UserInvalidVerificationTokenException.class,
        UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleUserException(
            BaseException exception, WebRequest request) {
        return buildErrorResponse(exception, request);
    }
}

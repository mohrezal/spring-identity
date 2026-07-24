package com.github.mohrezal.identity.domain.user.exception.context;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;

public record RegistrationAuditExceptionContext(
        AuditRequestContext auditRequestContext, String attemptedEmail)
        implements ExceptionContext {

    @Override
    public String toString() {
        return "REGISTRATION_AUDIT";
    }
}

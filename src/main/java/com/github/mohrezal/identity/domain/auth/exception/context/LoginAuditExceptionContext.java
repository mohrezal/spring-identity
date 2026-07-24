package com.github.mohrezal.identity.domain.auth.exception.context;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;

public record LoginAuditExceptionContext(
        AuditRequestContext auditRequestContext, String attemptedEmail)
        implements ExceptionContext {

    @Override
    public String toString() {
        return "LOGIN_AUDIT";
    }
}

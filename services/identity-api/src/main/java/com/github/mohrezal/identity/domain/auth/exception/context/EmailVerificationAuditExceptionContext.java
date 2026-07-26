package com.github.mohrezal.identity.domain.auth.exception.context;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.shared.exception.context.ExceptionContext;

public record EmailVerificationAuditExceptionContext(
        AuditRequestContext auditRequestContext, String email) implements ExceptionContext {

    @Override
    public String toString() {
        return "EMAIL_VERIFICATION_AUDIT";
    }
}

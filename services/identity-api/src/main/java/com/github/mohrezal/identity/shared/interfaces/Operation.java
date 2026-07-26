package com.github.mohrezal.identity.shared.interfaces;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;

interface Operation<P, R> {
    default void validate(P params) {}

    R execute(P params, AuditRequestContext auditRequestContext);
}

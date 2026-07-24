package com.github.mohrezal.identity.audit.contract;

import java.util.UUID;

public record AuditSubject(UUID userId, String email) {}

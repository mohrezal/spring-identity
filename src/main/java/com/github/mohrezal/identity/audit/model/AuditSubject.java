package com.github.mohrezal.identity.audit.model;

import java.util.UUID;

public record AuditSubject(UUID userId, String email) {}

package com.github.mohrezal.identity.audit.enums;

public enum AuditOutcome {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE");

    private final String name;

    AuditOutcome(String name) {
        this.name = name;
    }
}

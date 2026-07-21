package com.github.mohrezal.identity.audit.enums;

public enum AuditEventType {
    LOGIN_SUCCEEDED("LOGIN_SUCCEEDED"),
    LOGIN_FAILED("LOGIN_FAILED");

    private final String name;

    AuditEventType(String name) {
        this.name = name;
    }
}

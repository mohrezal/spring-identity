package com.github.mohrezal.identity.audit.enums;

public enum AuditEventType {
    LOGIN_SUCCEEDED("LOGIN_SUCCEEDED"),
    LOGIN_FAILED("LOGIN_FAILED"),
    REGISTER_STARTED("REGISTER_STARTED"),
    REGISTER_SUCCEEDED("REGISTER_SUCCEEDED"),
    REGISTER_FAILED("REGISTER_FAILED"),
    EMAIL_VERIFICATION_SENT("EMAIL_VERIFICATION_SENT"),
    EMAIL_VERIFIED("EMAIL_VERIFIED"),
    EMAIL_VERIFICATION_FAILED("EMAIL_VERIFICATION_FAILED");

    private final String name;

    AuditEventType(String name) {
        this.name = name;
    }
}

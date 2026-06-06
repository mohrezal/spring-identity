package com.github.mohrezal.identity.shared.constant;

public final class RegexPatterns {
    private RegexPatterns() {}

    public static final String NAME_PATTERN = "^[\\p{L} '-]+$";
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$";
}

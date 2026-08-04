package com.github.mohrezal.identity.shared.service;

import java.util.Locale;

public final class EmailAddressNormalizer {

    private EmailAddressNormalizer() {}

    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}

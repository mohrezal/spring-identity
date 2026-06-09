package com.github.mohrezal.identity.config;

public final class RouteConstants {
    private static final String API_BASE = "/api/";

    public static String build(String... segments) {
        return String.join("/", segments);
    }

    public static final class User {
        public static final String BASE = API_BASE + "users";
        public static final String REGISTER = "register";
    }

    public static final class Auth {
        public static final String BASE = API_BASE + "auth";
        public static final String CSRF = "csrf";
        public static final String VERIFY_EMAIL = "verify-email";
    }
}

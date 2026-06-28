package com.github.mohrezal.identity.config;

public final class RouteConstants {
    private static final String API_BASE = "/api/";

    public static String build(String... segments) {
        return String.join("/", segments);
    }

    public static final class User {
        public static final String BASE = API_BASE + "users";
        public static final String REGISTER = "register";
        public static final String ME = "me";
    }

    public static final class Auth {
        public static final String BASE = API_BASE + "auth";
        public static final String CSRF = "csrf";
        public static final String VERIFY_EMAIL = "verify-email";
        public static final String RESEND_EMAIL_VERIFICATION = "resend-email-verification";
        public static final String LOGIN = "login";
        public static final String LOGOUT = "logout";
        public static final String LOGOUT_ALL = "logout-all";
        public static final String REFRESH = "refresh";
        public static final String CHANGE_PASSWORD = "change-password";
        public static final String FORGOT_PASSWORD = "forgot-password";
        public static final String RESET_PASSWORD = "reset-password";
        public static final String SESSIONS = "sessions";

        public static final class OAuth {
            public static final String BASE = Auth.BASE + "/o";
            public static final String CONNECTIONS = "connections";
            public static final String CONNECTION = "connections/{id}";
            public static final String AUTHORIZE = "{provider}/authorize";
            public static final String LINK = "{provider}/link";
            public static final String CALLBACK = "{provider}/callback";
        }
    }

    public static final class Privilege {
        public static final String PERMISSIONS = API_BASE + "permissions";
        public static final String ROLES = API_BASE + "roles";
        public static final String ROLE = "{id}";
        public static final String ROLE_ASSIGNMENTS = "assignments/{userId}";
    }
}

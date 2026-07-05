package com.github.mohrezal.identity.support.data;

public final class TestConstants {

    private TestConstants() {}

    public static final class Origin {
        private Origin() {}

        public static final String CLIENT = "https://client.test";
        public static final String LOCAL = "http://localhost:3000";
    }

    public static final class Redirect {
        private Redirect() {}

        public static final String PASSWORD_RESET = Origin.CLIENT + "/reset-password";
    }

    public static final class RequestMetadata {
        private RequestMetadata() {}

        public static final String IP_ADDRESS = "192.0.2.10";
        public static final String OTHER_IP_ADDRESS = "192.0.2.11";
        public static final String USER_AGENT = "Test Browser";
        public static final String OTHER_USER_AGENT = "Replacement Browser";
    }

    public static final class Account {
        private Account() {}

        public static final String EMAIL = "user@client.test";
        public static final String PASSWORD = "Password1!";
        public static final String NEW_PASSWORD = "NewPassword1!";
    }
}

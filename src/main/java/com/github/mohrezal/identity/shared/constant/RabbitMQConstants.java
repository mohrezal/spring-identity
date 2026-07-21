package com.github.mohrezal.identity.shared.constant;

public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    public static final class Header {

        public static final String MESSAGE_ID = "x-message-id";

        private Header() {}
    }

    public static final class Notification {

        public static final String EXCHANGE = "notification.exchange";
        public static final int MAX_PRIORITY = 10;

        private Notification() {}

        public static final class Queue {

            public static final String EMAIL = "notification.email.queue";
            public static final String OAUTH_WELCOME_EMAIL =
                    "notification.oauth.welcome.email.queue";
            public static final String OAUTH_LINK_EMAIL = "notification.oauth.link.email.queue";
            public static final String PASSWORD_RESET_EMAIL =
                    "notification.password.reset.email.queue";

            private Queue() {}
        }

        public static final class RoutingKey {

            public static final String EMAIL = "notification.email";
            public static final String TRANSACTIONAL_EMAIL =
                    "notification.transactional.email.route";
            public static final String OAUTH_WELCOME_EMAIL =
                    "notification.oauth.welcome.email.route";
            public static final String OAUTH_LINK_EMAIL = "notification.oauth.link.email.route";
            public static final String PASSWORD_RESET_EMAIL =
                    "notification.password.reset.email.route";

            private RoutingKey() {}
        }

        public static final class Priority {

            public static final int OAUTH_WELCOME_EMAIL = 3;
            public static final int OAUTH_LINK_EMAIL = 7;
            public static final int PASSWORD_RESET_EMAIL = 9;

            private Priority() {}
        }
    }

    public static final class Audit {

        public static final String EXCHANGE = "audit.exchange";

        private Audit() {}

        public static final class Queue {

            public static final String AUDIT = "audit.event.queue";

            private Queue() {}
        }

        public static final class RoutingKey {

            public static final String AUDIT = "audit.event.route";

            private RoutingKey() {}
        }
    }

    public static final class DeadLetter {

        public static final String EXCHANGE = "dead.letter.exchange";

        private DeadLetter() {}

        public static final class Queue {

            public static final String EMAIL = "dead.letter.email.queue";

            private Queue() {}
        }

        public static final class RoutingKey {

            public static final String EMAIL = "dead.letter.email.route";

            private RoutingKey() {}
        }
    }
}

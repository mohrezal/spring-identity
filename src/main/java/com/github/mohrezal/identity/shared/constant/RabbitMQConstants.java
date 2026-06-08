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

            private Queue() {}
        }

        public static final class RoutingKey {

            public static final String EMAIL = "notification.email";
            public static final String TRANSACTIONAL_EMAIL =
                    "notification.transactional.email.route";

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

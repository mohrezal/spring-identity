package com.github.mohrezal.identity.domain.user.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.mohrezal.identity.domain.user.listener.message.UserEmailVerificationMessage;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import com.github.mohrezal.identity.shared.rabbitmq.RabbitMQPublisher;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    private static final String EMAIL = "user@client.test";
    private static final String ACTIVATION_URL =
            "https://client.test/verify-email?token=11111111-1111-1111-1111-111111111111";

    @Mock
    private RabbitMQPublisher rabbitMQPublisher;

    @InjectMocks
    private UserEventListener listener;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(UserEventListener.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void handle_publishesMessageAndLogsOnlyUserId() {
        var userId = UUID.randomUUID();
        var message = new UserEmailVerificationMessage(userId, EMAIL, ACTIVATION_URL);

        listener.handle(message);

        verify(rabbitMQPublisher)
                .publish(
                        RabbitMQConstants.Notification.EXCHANGE,
                        RabbitMQConstants.Notification.RoutingKey.TRANSACTIONAL_EMAIL,
                        message);

        assertThat(logAppender.list)
                .isNotEmpty()
                .allSatisfy(
                        event -> {
                            assertThat(event.getLevel()).isEqualTo(Level.INFO);
                            var formatted = event.getFormattedMessage();
                            assertThat(formatted).contains(userId.toString());
                            assertThat(formatted).doesNotContain(EMAIL);
                            assertThat(formatted).doesNotContain(ACTIVATION_URL);
                            assertThat(formatted).doesNotContain("token=");
                        });
    }
}

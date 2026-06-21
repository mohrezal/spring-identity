package com.github.mohrezal.identity.domain.authentication.listener;

import com.github.mohrezal.identity.domain.authentication.listener.message.OAuthLinkEmailMessage;
import com.github.mohrezal.identity.domain.authentication.listener.message.OAuthWelcomeEmailMessage;
import com.github.mohrezal.identity.domain.authentication.listener.message.PasswordResetEmailMessage;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import com.github.mohrezal.identity.shared.rabbitmq.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthNotificationListener {

    private final RabbitMQPublisher rabbitMQPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OAuthWelcomeEmailMessage message) {
        rabbitMQPublisher.publish(
                RabbitMQConstants.Notification.EXCHANGE,
                RabbitMQConstants.Notification.RoutingKey.OAUTH_WELCOME_EMAIL,
                message);

        log.info(
                "Published OAuth welcome email message. userId={}, provider={}",
                message.userId(),
                message.provider());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OAuthLinkEmailMessage message) {
        rabbitMQPublisher.publish(
                RabbitMQConstants.Notification.EXCHANGE,
                RabbitMQConstants.Notification.RoutingKey.OAUTH_LINK_EMAIL,
                message);

        log.info(
                "Published OAuth link email message. userId={}, provider={}",
                message.userId(),
                message.provider());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PasswordResetEmailMessage message) {
        rabbitMQPublisher.publish(
                RabbitMQConstants.Notification.EXCHANGE,
                RabbitMQConstants.Notification.RoutingKey.PASSWORD_RESET_EMAIL,
                message,
                RabbitMQConstants.Notification.Priority.PASSWORD_RESET_EMAIL);

        log.info("Published password reset email message. userId={}", message.userId());
    }
}

package com.github.mohrezal.identity.domain.user.listener;

import com.github.mohrezal.identity.domain.user.listener.message.UserEmailVerificationMessage;
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
public class UserEventListener {

    private final RabbitMQPublisher rabbitMQPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserEmailVerificationMessage message) {
        rabbitMQPublisher.publish(
                RabbitMQConstants.Notification.EXCHANGE,
                RabbitMQConstants.Notification.RoutingKey.TRANSACTIONAL_EMAIL,
                message);

        log.info(
                "Publishing email verification message for email={}, activationUrl={}",
                message.to(),
                message.activationUrl());
    }
}

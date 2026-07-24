package com.github.mohrezal.identity.shared.rabbitmq;

import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String exchange, String routingKey, Object message) {
        publish(exchange, routingKey, message, null);
    }

    public void publish(String exchange, String routingKey, Object message, Integer priority) {
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                message,
                rabbitMessage -> {
                    var messageProperties = rabbitMessage.getMessageProperties();
                    var messageId = UUID.randomUUID().toString();
                    messageProperties.setMessageId(messageId);
                    messageProperties.setHeader(RabbitMQConstants.Header.MESSAGE_ID, messageId);
                    if (priority != null) {
                        messageProperties.setPriority(priority);
                    }
                    return rabbitMessage;
                });
    }
}

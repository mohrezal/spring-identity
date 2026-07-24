package com.github.mohrezal.identity.shared.rabbitmq.config;

import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

public abstract class RabbitMQBaseConfig {

    protected Queue queue(String name, String dlqRoutingKey) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(RabbitMQConstants.DeadLetter.EXCHANGE)
                .deadLetterRoutingKey(dlqRoutingKey)
                .lazy()
                .build();
    }

    protected Queue priorityQueue(String name, int maxPriority, String dlqRoutingKey) {
        return QueueBuilder.durable(name)
                .maxPriority(maxPriority)
                .deadLetterExchange(RabbitMQConstants.DeadLetter.EXCHANGE)
                .deadLetterRoutingKey(dlqRoutingKey)
                .lazy()
                .build();
    }

    protected Queue dlq(String name) {
        return QueueBuilder.durable(name).lazy().build();
    }

    protected Binding bind(Queue queue, DirectExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}

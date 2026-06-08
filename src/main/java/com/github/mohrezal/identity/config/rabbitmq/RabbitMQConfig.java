package com.github.mohrezal.identity.config.rabbitmq;

import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(RabbitMQConstants.Notification.EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(RabbitMQConstants.Notification.Queue.EMAIL)
                .maxPriority(RabbitMQConstants.Notification.MAX_PRIORITY)
                .deadLetterExchange(RabbitMQConstants.DeadLetter.EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.DeadLetter.RoutingKey.EMAIL)
                .lazy()
                .build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with(RabbitMQConstants.Notification.RoutingKey.EMAIL);
    }

    @Bean
    public Binding transactionalEmailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with(RabbitMQConstants.Notification.RoutingKey.TRANSACTIONAL_EMAIL);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitMQConstants.DeadLetter.EXCHANGE);
    }

    @Bean
    public Queue deadLetterEmailQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DeadLetter.Queue.EMAIL).lazy().build();
    }

    @Bean
    public Binding deadLetterEmailBinding() {
        return BindingBuilder.bind(deadLetterEmailQueue())
                .to(deadLetterExchange())
                .with(RabbitMQConstants.DeadLetter.RoutingKey.EMAIL);
    }

    @Bean
    public MessageConverter messageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}

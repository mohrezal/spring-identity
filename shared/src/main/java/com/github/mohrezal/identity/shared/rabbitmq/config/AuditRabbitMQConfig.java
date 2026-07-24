package com.github.mohrezal.identity.shared.rabbitmq.config;

import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditRabbitMQConfig extends RabbitMQBaseConfig {

    @Bean
    public Queue auditQueue() {
        return queue(
                RabbitMQConstants.Audit.Queue.AUDIT, RabbitMQConstants.DeadLetter.RoutingKey.AUDIT);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange auditExchange) {
        return bind(auditQueue, auditExchange, RabbitMQConstants.Audit.RoutingKey.AUDIT);
    }
}

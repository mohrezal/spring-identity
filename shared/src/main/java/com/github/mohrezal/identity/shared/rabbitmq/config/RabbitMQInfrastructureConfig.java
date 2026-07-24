package com.github.mohrezal.identity.shared.rabbitmq.config;

import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import java.time.Duration;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQInfrastructureConfig extends RabbitMQBaseConfig {

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(RabbitMQConstants.Notification.EXCHANGE);
    }

    @Bean
    public DirectExchange auditExchange() {
        return new DirectExchange(RabbitMQConstants.Audit.EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitMQConstants.DeadLetter.EXCHANGE);
    }

    @Bean
    public Queue deadLetterAuditQueue() {
        return dlq(RabbitMQConstants.DeadLetter.Queue.AUDIT);
    }

    @Bean
    public Queue deadLetterEmailQueue() {
        return dlq(RabbitMQConstants.DeadLetter.Queue.EMAIL);
    }

    @Bean
    public Binding deadLetterAuditBinding(
            Queue deadLetterAuditQueue, DirectExchange deadLetterExchange) {
        return bind(
                deadLetterAuditQueue,
                deadLetterExchange,
                RabbitMQConstants.DeadLetter.RoutingKey.AUDIT);
    }

    @Bean
    public Binding deadLetterEmailBinding(
            Queue deadLetterEmailQueue, DirectExchange deadLetterExchange) {
        return bind(
                deadLetterEmailQueue,
                deadLetterExchange,
                RabbitMQConstants.DeadLetter.RoutingKey.EMAIL);
    }

    @Bean
    public MessageConverter messageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        var interceptor =
                RetryInterceptorBuilder.stateless()
                        .configureRetryPolicy(
                                builder ->
                                        builder.maxRetries(2)
                                                .delay(Duration.ofSeconds(1))
                                                .multiplier(2.0)
                                                .maxDelay(Duration.ofSeconds(10))
                                                .excludes(AmqpRejectAndDontRequeueException.class))
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build();

        factory.setAdviceChain(interceptor);
        return factory;
    }
}

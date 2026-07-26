package com.github.mohrezal.identity.shared.rabbitmq.config;

import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitMQConfig extends RabbitMQBaseConfig {

    @Bean
    public Queue emailQueue() {
        return priorityQueue(
                RabbitMQConstants.Notification.Queue.EMAIL,
                RabbitMQConstants.Notification.MAX_PRIORITY,
                RabbitMQConstants.DeadLetter.RoutingKey.EMAIL);
    }

    @Bean
    public Queue oAuthWelcomeEmailQueue() {
        return priorityQueue(
                RabbitMQConstants.Notification.Queue.OAUTH_WELCOME_EMAIL,
                RabbitMQConstants.Notification.MAX_PRIORITY,
                RabbitMQConstants.DeadLetter.RoutingKey.EMAIL);
    }

    @Bean
    public Queue oAuthLinkEmailQueue() {
        return priorityQueue(
                RabbitMQConstants.Notification.Queue.OAUTH_LINK_EMAIL,
                RabbitMQConstants.Notification.MAX_PRIORITY,
                RabbitMQConstants.DeadLetter.RoutingKey.EMAIL);
    }

    @Bean
    public Queue passwordResetEmailQueue() {
        return priorityQueue(
                RabbitMQConstants.Notification.Queue.PASSWORD_RESET_EMAIL,
                RabbitMQConstants.Notification.MAX_PRIORITY,
                RabbitMQConstants.DeadLetter.RoutingKey.EMAIL);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange notificationExchange) {
        return bind(
                emailQueue, notificationExchange, RabbitMQConstants.Notification.RoutingKey.EMAIL);
    }

    @Bean
    public Binding transactionalEmailBinding(
            Queue emailQueue, DirectExchange notificationExchange) {
        return bind(
                emailQueue,
                notificationExchange,
                RabbitMQConstants.Notification.RoutingKey.TRANSACTIONAL_EMAIL);
    }

    @Bean
    public Binding oAuthWelcomeEmailBinding(
            Queue oAuthWelcomeEmailQueue, DirectExchange notificationExchange) {
        return bind(
                oAuthWelcomeEmailQueue,
                notificationExchange,
                RabbitMQConstants.Notification.RoutingKey.OAUTH_WELCOME_EMAIL);
    }

    @Bean
    public Binding oAuthLinkEmailBinding(
            Queue oAuthLinkEmailQueue, DirectExchange notificationExchange) {
        return bind(
                oAuthLinkEmailQueue,
                notificationExchange,
                RabbitMQConstants.Notification.RoutingKey.OAUTH_LINK_EMAIL);
    }

    @Bean
    public Binding passwordResetEmailBinding(
            Queue passwordResetEmailQueue, DirectExchange notificationExchange) {
        return bind(
                passwordResetEmailQueue,
                notificationExchange,
                RabbitMQConstants.Notification.RoutingKey.PASSWORD_RESET_EMAIL);
    }
}

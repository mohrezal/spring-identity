package com.github.mohrezal.identity.support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfiguration {

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("identity")
                .withUsername("identity")
                .withPassword("identity");
    }

    @Bean
    @ServiceConnection(name = "redis")
    @SuppressWarnings("resource")
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    }

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    RabbitMQContainer rabbitMq() {
        return new RabbitMQContainer("rabbitmq:4.2.3-management");
    }
}

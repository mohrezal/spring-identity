package com.github.mohrezal.identity.audit.consumer;

import com.github.mohrezal.identity.audit.contract.AuditEvent;
import com.github.mohrezal.identity.audit.validation.AuditSchemaValidator;
import com.github.mohrezal.identity.shared.constant.RabbitMQConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditMessageConsumer {

    private final AuditSchemaValidator schemaValidator;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @RabbitListener(queues = RabbitMQConstants.Audit.Queue.AUDIT)
    public void consume(AuditEvent event) {
        meterRegistry.counter("audit.events.received").increment();

        var jsonPayload = objectMapper.writeValueAsString(event);
        var violations = schemaValidator.validate(jsonPayload);

        if (!violations.isEmpty()) {
            log.warn("Validation failed eventId={}, violations={}", event.eventId(), violations);
            meterRegistry
                    .counter(
                            "audit.events.validation.failed", "eventType", event.eventType().name())
                    .increment();
            throw new AmqpRejectAndDontRequeueException("Schema validation failed");
        }

        log.atInfo()
                .setMessage("Audit event processed")
                .addKeyValue("eventId", event.eventId())
                .addKeyValue("eventType", event.eventType().name())
                .addKeyValue("outcome", event.outcome().name())
                .addKeyValue(
                        "actor",
                        event.actor() != null && event.actor().userId() != null
                                ? event.actor().userId().toString()
                                : null)
                .addKeyValue("traceId", event.traceId())
                .log();

        meterRegistry
                .counter(
                        "audit.events.emitted",
                        "eventType",
                        event.eventType().name(),
                        "outcome",
                        event.outcome().name())
                .increment();
    }
}

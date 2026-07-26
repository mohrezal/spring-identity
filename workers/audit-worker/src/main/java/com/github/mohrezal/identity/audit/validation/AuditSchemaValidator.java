package com.github.mohrezal.identity.audit.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditSchemaValidator {

    private static final String SCHEMA_PATH = "/contracts/audit/v1/audit-event.schema.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchema schema;

    public AuditSchemaValidator() {
        try (InputStream is = getClass().getResourceAsStream(SCHEMA_PATH)) {
            if (is == null) {
                throw new IOException("Schema not found: " + SCHEMA_PATH);
            }
            var schemaNode = mapper.readTree(is);
            var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            this.schema = factory.getSchema(schemaNode);
            log.info("Loaded JSON schema: {}", SCHEMA_PATH);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load JSON schema: " + SCHEMA_PATH, e);
        }
    }

    public Set<ValidationMessage> validate(String jsonPayload) {
        try {
            var document = mapper.readTree(jsonPayload);
            return schema.validate(document);
        } catch (IOException e) {
            log.error("Failed to parse payload as JSON", e);
            throw new RuntimeException("Invalid JSON payload", e);
        }
    }
}

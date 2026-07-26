package com.github.mohrezal.identity.contract.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class AuditContractValidator {

    private static final Path CONTRACT_ROOT = Path.of("contracts", "audit", "v1");
    private static final Path SCHEMA_FILE = CONTRACT_ROOT.resolve("audit-event.schema.json");
    private static final Path EXAMPLES_DIR = CONTRACT_ROOT.resolve("examples");
    private static final Path INVALID_EXAMPLES_DIR = CONTRACT_ROOT.resolve("invalid-examples");

    private final ObjectMapper mapper;
    private final JsonSchema schema;

    private AuditContractValidator() throws IOException {
        this.mapper = new ObjectMapper();
        var schemaNode = mapper.readTree(SCHEMA_FILE.toFile());
        var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.schema = factory.getSchema(schemaNode);
    }

    public static void main(String[] args) throws IOException {
        var validator = new AuditContractValidator();
        if (validator.run() != 0) {
            throw new RuntimeException("Contract validation failed");
        }
    }

    private int run() throws IOException {
        var exitCode = 0;

        System.out.println("Schema");
        System.out.println("  PASS " + SCHEMA_FILE.getFileName());

        System.out.println();
        exitCode += validateValidExamples();

        System.out.println();
        exitCode += validateInvalidExamples();

        System.out.println();
        if (exitCode > 0) {
            System.out.println("Contract validation failed with " + exitCode + " error(s).");
            return 1;
        }

        System.out.println("Contract validation passed.");
        return 0;
    }

    private int validateValidExamples() throws IOException {
        System.out.println("Valid examples");

        var files = sortedJsonFiles(EXAMPLES_DIR);
        if (files.isEmpty()) {
            System.out.println("  FAIL no valid examples found");
            return 1;
        }

        var failures = 0;
        for (var file : files) {
            var document = mapper.readTree(file.toFile());
            var schemaErrors = schema.validate(document);

            var semanticErrors =
                    schemaErrors.isEmpty() ? validateSemantics(document) : List.<String>of();

            if (!schemaErrors.isEmpty() || !semanticErrors.isEmpty()) {
                failures++;
                System.out.println("  FAIL " + file.getFileName());
                for (var error : schemaErrors) {
                    System.out.println("       " + formatError(error));
                }
                for (var error : semanticErrors) {
                    System.out.println("       " + error);
                }
            } else {
                System.out.println("  PASS " + file.getFileName());
            }
        }

        return failures > 0 ? 1 : 0;
    }

    private int validateInvalidExamples() throws IOException {
        System.out.println("Invalid examples");

        var files = sortedJsonFiles(INVALID_EXAMPLES_DIR);
        if (files.isEmpty()) {
            System.out.println("  FAIL no invalid examples found");
            return 1;
        }

        var failures = 0;
        for (var file : files) {
            var document = mapper.readTree(file.toFile());
            var schemaErrors = schema.validate(document);

            var semanticErrors =
                    schemaErrors.isEmpty() ? validateSemantics(document) : List.<String>of();

            if (!schemaErrors.isEmpty() || !semanticErrors.isEmpty()) {
                System.out.println("  PASS " + file.getFileName() + " rejected");
            } else {
                failures++;
                System.out.println("  FAIL " + file.getFileName() + " was unexpectedly accepted");
            }
        }

        return failures > 0 ? 1 : 0;
    }

    private List<String> validateSemantics(JsonNode document) {
        var errors = new ArrayList<String>();

        if (!document.isObject()) {
            return errors;
        }

        var traceId = document.path("traceId").asText(null);
        var request = document.path("request");

        if (request.isObject()) {
            var requestId = request.path("requestId").asText(null);
            if (requestId != null && traceId != null && !requestId.equals(traceId)) {
                errors.add("$.request.requestId must equal $.traceId");
            }
        }

        return errors;
    }

    private String formatError(com.networknt.schema.ValidationMessage error) {
        return error.getMessage();
    }

    private List<Path> sortedJsonFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }
    }
}

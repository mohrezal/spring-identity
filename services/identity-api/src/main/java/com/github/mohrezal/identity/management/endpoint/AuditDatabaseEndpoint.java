package com.github.mohrezal.identity.management.endpoint;

import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "audit-database")
@RequiredArgsConstructor
public class AuditDatabaseEndpoint {

    private final DataSource dataSource;

    @ReadOperation
    public Map<String, Object> health() {
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute("SELECT 1");
            }
            return Map.of(
                    "status", "UP", "database", connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            return Map.of("status", "DOWN", "error", e.getMessage());
        }
    }
}

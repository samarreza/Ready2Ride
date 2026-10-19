package com.ready2ride.core.database;

import com.ready2ride.core.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Reusable database connection manager for PostgreSQL.
 * Provides on-demand JDBC connections designed for try-with-resources management.
 * Guarantees that credentials are never logged or propagated in error messages.
 */
public class DatabaseConnectionManager {

    private final DatabaseConfig config;

    /**
     * Initializes the manager with explicit database configuration.
     *
     * @param config database configuration parameters
     */
    public DatabaseConnectionManager(DatabaseConfig config) {
        this.config = Objects.requireNonNull(config, "DatabaseConfig must not be null");
    }

    /**
     * Initializes the manager using system environment variables.
     */
    public DatabaseConnectionManager() {
        this(DatabaseConfig.fromEnvironment());
    }

    /**
     * Obtains a fresh database connection. Callers are responsible for closing
     * the connection, typically using a try-with-resources block.
     *
     * @return active {@link Connection}
     * @throws DatabaseConnectionException if connection could not be established
     */
    public Connection getConnection() {
        try {
            if (config.getUser() != null && config.getPassword() != null) {
                return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            } else if (config.getUser() != null) {
                return DriverManager.getConnection(config.getUrl(), config.getUser(), "");
            } else {
                return DriverManager.getConnection(config.getUrl());
            }
        } catch (SQLException e) {
            String sanitizedMessage = String.format(
                    "Failed to connect to PostgreSQL database at URL '%s' (User: '%s'). SQLState: %s, ErrorCode: %d",
                    config.getUrl(),
                    (config.getUser() != null ? config.getUser() : "<none>"),
                    e.getSQLState(),
                    e.getErrorCode()
            );
            throw new DatabaseConnectionException(sanitizedMessage, e);
        }
    }

    /**
     * Validates whether the configured PostgreSQL database is accessible and responsive.
     *
     * @param timeoutSeconds maximum wait duration for the connection probe
     * @return true if connection succeeds; false otherwise
     */
    public boolean isDatabaseAvailable(int timeoutSeconds) {
        try (Connection conn = getConnection()) {
            return conn.isValid(Math.max(1, timeoutSeconds));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convenience check using a default timeout of 2 seconds.
     *
     * @return true if accessible
     */
    public boolean isDatabaseAvailable() {
        return isDatabaseAvailable(2);
    }

    public DatabaseConfig getConfig() {
        return config;
    }
}

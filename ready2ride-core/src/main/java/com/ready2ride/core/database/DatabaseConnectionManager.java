
package com.ready2ride.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import com.ready2ride.core.exception.DatabaseConnectionException;

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
        this.config = Objects.requireNonNull(
                config, "DatabaseConfig must not be null"
        );
    }

    /**
     * Initializes the manager using system environment variables.
     */
    public DatabaseConnectionManager() {
        this(DatabaseConfig.fromEnvironment());
    }

    /**
     * Obtains a fresh database connection.
     *
     * @return active database connection
     * @throws DatabaseConnectionException if connection cannot be established
     */
    public Connection getConnection() {
        try {
            // Explicitly load the PostgreSQL JDBC driver.
            Class.forName("org.postgresql.Driver");

            if (config.getUser() != null && config.getPassword() != null) {
                return DriverManager.getConnection(
                        config.getUrl(),
                        config.getUser(),
                        config.getPassword()
                );
            } else if (config.getUser() != null) {
                return DriverManager.getConnection(
                        config.getUrl(),
                        config.getUser(),
                        ""
                );
            } else {
                return DriverManager.getConnection(config.getUrl());
            }

        } catch (ClassNotFoundException e) {
            throw new DatabaseConnectionException(
                    "PostgreSQL JDBC driver not found. " +
                    "Ensure the PostgreSQL JDBC dependency is available at runtime.",
                    e
            );

        } catch (SQLException e) {
            String sanitizedMessage = String.format(
                    "Failed to connect to PostgreSQL database at URL '%s' " +
                    "(User: '%s'). SQLState: %s, ErrorCode: %d",
                    config.getUrl(),
                    config.getUser() != null
                            ? config.getUser()
                            : "<none>",
                    e.getSQLState(),
                    e.getErrorCode()
            );

            throw new DatabaseConnectionException(sanitizedMessage, e);
        }
    }

    /**
     * Checks whether the database is accessible and responsive.
     *
     * @param timeoutSeconds maximum wait duration in seconds
     * @return true if the connection succeeds; false otherwise
     */
    public boolean isDatabaseAvailable(int timeoutSeconds) {
        try (Connection conn = getConnection()) {
            return conn.isValid(Math.max(1, timeoutSeconds));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks database availability using a two-second timeout.
     *
     * @return true if the database is accessible
     */
    public boolean isDatabaseAvailable() {
        return isDatabaseAvailable(2);
    }

    public DatabaseConfig getConfig() {
        return config;
    }
}

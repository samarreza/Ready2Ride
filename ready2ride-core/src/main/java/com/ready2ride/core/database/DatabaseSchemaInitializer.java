package com.ready2ride.core.database;

import com.ready2ride.core.exception.DataAccessException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Utility for executing DDL schema definitions and development seed scripts
 * against an active PostgreSQL connection.
 */
public class DatabaseSchemaInitializer {

    private final DatabaseConnectionManager connectionManager;

    public DatabaseSchemaInitializer(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    /**
     * Executes an SQL script file located on disk.
     *
     * @param scriptPath path to the SQL file
     */
    public void executeScriptFile(Path scriptPath) {
        Objects.requireNonNull(scriptPath, "Script path must not be null");
        try {
            String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);
            executeSqlScript(sql);
        } catch (Exception e) {
            throw new DataAccessException("Failed to execute script file: " + scriptPath, e);
        }
    }

    /**
     * Executes raw SQL script content containing one or more statements separated by semicolons.
     *
     * @param scriptContent raw SQL statements
     */
    public void executeSqlScript(String scriptContent) {
        if (scriptContent == null || scriptContent.isBlank()) {
            return;
        }

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Strip out full-line comments and split statements by semicolon
            StringBuilder currentStatement = new StringBuilder();
            String[] lines = scriptContent.split("\\R");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                currentStatement.append(line).append(" ");
                if (trimmed.endsWith(";")) {
                    stmt.execute(currentStatement.toString());
                    currentStatement.setLength(0);
                }
            }

            if (!currentStatement.toString().trim().isEmpty()) {
                stmt.execute(currentStatement.toString());
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error executing SQL script statement: " + e.getMessage(), e);
        }
    }
}

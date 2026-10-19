package com.ready2ride.core.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @Test
    @DisplayName("Should use default PostgreSQL URL when null or blank URL is provided")
    void testDefaultDatabaseUrl() {
        DatabaseConfig config = new DatabaseConfig(null, null, null);
        assertEquals(DatabaseConfig.DEFAULT_DB_URL, config.getUrl());

        DatabaseConfig blankConfig = new DatabaseConfig("   ", "user", "pass");
        assertEquals(DatabaseConfig.DEFAULT_DB_URL, blankConfig.getUrl());
    }

    @Test
    @DisplayName("Should detect presence or absence of credentials")
    void testHasCredentials() {
        DatabaseConfig noCreds = new DatabaseConfig(null, null, null);
        assertFalse(noCreds.hasCredentials());

        DatabaseConfig userOnly = new DatabaseConfig(null, "postgres", null);
        assertFalse(userOnly.hasCredentials());

        DatabaseConfig fullCreds = new DatabaseConfig("jdbc:postgresql://localhost:5432/ready2ride", "postgres", "secret123");
        assertTrue(fullCreds.hasCredentials());
        assertEquals("postgres", fullCreds.getUser());
        assertEquals("secret123", fullCreds.getPassword());
    }

    @Test
    @DisplayName("Should redact password in toString to prevent log credential leaks")
    void testRedactedToString() {
        DatabaseConfig config = new DatabaseConfig("jdbc:postgresql://localhost:5432/ready2ride", "postgres", "mySecretPassword");
        String stringRep = config.toString();

        assertFalse(stringRep.contains("mySecretPassword"), "toString must never expose plaintext password");
        assertTrue(stringRep.contains("********"), "toString must display masked placeholder for password");
        assertTrue(stringRep.contains("postgres"));
        assertTrue(stringRep.contains("ready2ride"));
    }

    @Test
    @DisplayName("Should load from environment variables safely")
    void testFromEnvironment() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        assertNotNull(config);
        assertNotNull(config.getUrl());
    }
}

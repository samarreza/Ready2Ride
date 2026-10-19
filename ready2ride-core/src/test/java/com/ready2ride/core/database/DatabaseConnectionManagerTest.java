package com.ready2ride.core.database;

import com.ready2ride.core.exception.DatabaseConnectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionManagerTest {

    @Test
    @DisplayName("Should instantiate manager with config and allow config retrieval")
    void testManagerInitialization() {
        DatabaseConfig config = new DatabaseConfig("jdbc:postgresql://localhost:5432/testdb", "testuser", "testpass");
        DatabaseConnectionManager manager = new DatabaseConnectionManager(config);

        assertNotNull(manager.getConfig());
        assertEquals("testuser", manager.getConfig().getUser());
    }

    @Test
    @DisplayName("Should fail gracefully and hide password when attempting to connect to non-existent endpoint")
    void testConnectionFailureMessageSanitization() {
        // Points to an invalid unreachable port
        DatabaseConfig badConfig = new DatabaseConfig("jdbc:postgresql://localhost:59999/nonexistent", "secretuser", "superSecretPassword");
        DatabaseConnectionManager manager = new DatabaseConnectionManager(badConfig);

        DatabaseConnectionException ex = assertThrows(DatabaseConnectionException.class, manager::getConnection);
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().contains("superSecretPassword"), "Exception message must never leak password");
        assertTrue(ex.getMessage().contains("secretuser"));
    }

    @Test
    @DisplayName("Should return false from isDatabaseAvailable when database is unreachable")
    void testIsDatabaseAvailableUnreachable() {
        DatabaseConfig badConfig = new DatabaseConfig("jdbc:postgresql://localhost:59999/nonexistent", "dummy", "dummy");
        DatabaseConnectionManager manager = new DatabaseConnectionManager(badConfig);

        assertFalse(manager.isDatabaseAvailable(1));
    }

    private void assertEquals(String expected, String actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}

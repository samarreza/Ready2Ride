package com.ready2ride.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    @DisplayName("Hashing a password produces standard PBKDF2 formatted string")
    void testHashPasswordFormat() {
        String hash = PasswordHasher.hashPassword("SuperSecret123!");
        assertNotNull(hash);
        assertTrue(hash.startsWith("PBKDF2$65536$"));

        String[] parts = hash.split("\\$");
        assertEqualsParts(4, parts.length);
        assertEqualsParts(32, parts[2].length()); // 16 bytes salt = 32 hex chars
        assertEqualsParts(64, parts[3].length()); // 32 bytes derived key = 64 hex chars
    }

    private void assertEqualsParts(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    @Test
    @DisplayName("Hashing the same password twice yields different hashes due to unique salts")
    void testUniqueSalts() {
        String hash1 = PasswordHasher.hashPassword("SamePassword123");
        String hash2 = PasswordHasher.hashPassword("SamePassword123");

        assertNotEquals(hash1, hash2, "Subsequent hashes of identical passwords must have unique salts");
        assertTrue(PasswordHasher.checkPassword("SamePassword123", hash1));
        assertTrue(PasswordHasher.checkPassword("SamePassword123", hash2));
    }

    @Test
    @DisplayName("Verifying valid candidate password returns true")
    void testCheckPasswordSuccess() {
        String password = "MyP@ssw0rd!2026";
        String hash = PasswordHasher.hashPassword(password);

        assertTrue(PasswordHasher.checkPassword(password, hash));
    }

    @Test
    @DisplayName("Verifying incorrect candidate password returns false")
    void testCheckPasswordFailure() {
        String hash = PasswordHasher.hashPassword("CorrectPassword123");

        assertFalse(PasswordHasher.checkPassword("WrongPassword123", hash));
        assertFalse(PasswordHasher.checkPassword("correctpassword123", hash)); // Case sensitive
        assertFalse(PasswordHasher.checkPassword("", hash));
        assertFalse(PasswordHasher.checkPassword(null, hash));
    }

    @Test
    @DisplayName("Rejecting null or blank passwords on hashing")
    void testRejectBlankPasswords() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hashPassword(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hashPassword("   "));
    }

    @Test
    @DisplayName("Gracefully handling corrupt or tampered hashes without throwing exceptions")
    void testHandleCorruptedHashes() {
        assertFalse(PasswordHasher.checkPassword("AnyPass", "not-a-valid-hash"));
        assertFalse(PasswordHasher.checkPassword("AnyPass", "PBKDF2$invalid$salt$hash"));
        assertFalse(PasswordHasher.checkPassword("AnyPass", null));
    }
}

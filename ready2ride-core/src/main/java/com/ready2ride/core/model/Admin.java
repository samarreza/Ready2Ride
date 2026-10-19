package com.ready2ride.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an administrator or staff member managing the fleet and operations.
 * Extends the unified {@link User} model with administrator-specific semantics.
 */
public class Admin extends User {

    private String username;

    /**
     * Constructs an Admin with Phase 1 attributes (backwards compatibility).
     *
     * @param id       unique administrator identifier
     * @param username username / alias
     * @param email    administrator contact/login email
     * @param fullName full legal name
     * @param role     role label string
     */
    public Admin(String id, String username, String email, String fullName, String role) {
        super(id, fullName, email, "+0-000-0000000", null, UserRole.ADMIN, null, true, LocalDateTime.now());
        this.username = Objects.requireNonNull(username, "Username must not be null");
    }

    /**
     * Constructs an Admin with complete account credentials and persistence attributes.
     */
    public Admin(String id, String username, String email, String fullName, String phoneNumber,
                 String passwordHash, boolean isActive, LocalDateTime createdAt) {
        super(id, fullName, email, phoneNumber, passwordHash, UserRole.ADMIN, null, isActive, createdAt);
        this.username = Objects.requireNonNull(username, "Username must not be null");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = Objects.requireNonNull(username, "Username must not be null");
    }

    @Override
    public String toString() {
        return String.format("Admin [id=%s, username=%s, email=%s, fullName=%s, active=%s]",
                getId(), username, getEmail(), getFullName(), isActive());
    }
}

package com.ready2ride.core.model;

/**
 * Enumeration of user account roles in the Ready2Ride system.
 * Strictly corresponds to the database check constraint: role IN ('CUSTOMER', 'ADMIN').
 */
public enum UserRole {
    CUSTOMER("Customer"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

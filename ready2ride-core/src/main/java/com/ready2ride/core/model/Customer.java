package com.ready2ride.core.model;

import java.time.LocalDateTime;

/**
 * Represents a registered customer using Ready2Ride.
 * Extends the unified {@link User} model with customer-specific semantics.
 */
public class Customer extends User {

    /**
     * Constructs a Customer with minimal attributes (Phase 1 backwards compatibility).
     *
     * @param id                   unique customer identifier
     * @param name                 customer's full name
     * @param email                unique customer email
     * @param phoneNumber          contact phone number
     * @param drivingLicenseNumber driver's license number
     */
    public Customer(String id, String name, String email, String phoneNumber, String drivingLicenseNumber) {
        super(id, name, email, phoneNumber, null, UserRole.CUSTOMER, drivingLicenseNumber, true, LocalDateTime.now());
    }

    /**
     * Constructs a Customer with full account credentials and persistence attributes.
     */
    public Customer(String id, String name, String email, String phoneNumber,
                    String passwordHash, String drivingLicenseNumber, boolean isActive, LocalDateTime createdAt) {
        super(id, name, email, phoneNumber, passwordHash, UserRole.CUSTOMER, drivingLicenseNumber, isActive, createdAt);
    }

    /**
     * Backwards-compatible alias for {@link #getFullName()}.
     */
    public String getName() {
        return getFullName();
    }

    /**
     * Backwards-compatible alias for {@link #setFullName(String)}.
     */
    public void setName(String name) {
        setFullName(name);
    }

    @Override
    public String toString() {
        return String.format("Customer [id=%s, name=%s, email=%s, phone=%s, dl=%s, active=%s]",
                getId(), getFullName(), getEmail(), getPhoneNumber(), getDrivingLicenseNumber(), isActive());
    }
}

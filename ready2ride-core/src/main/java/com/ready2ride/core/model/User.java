package com.ready2ride.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Unified user account entity representing records in the {@code users} table.
 * Supports both customer accounts and fleet administrators, differentiated by {@link UserRole}.
 */
public class User {

    private final String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private UserRole role;
    private String drivingLicenseNumber;
    private boolean isActive;
    private LocalDateTime createdAt;

    /**
     * Constructs a fully specified User account entity.
     *
     * @param id                   unique user identifier
     * @param fullName             user's full legal name
     * @param email                unique email address
     * @param phoneNumber          contact phone number
     * @param passwordHash         cryptographic password hash
     * @param role                 {@link UserRole} classification (CUSTOMER or ADMIN)
     * @param drivingLicenseNumber optional driver's license number (for customers)
     * @param isActive             account active status
     * @param createdAt            timestamp of account creation
     */
    public User(String id, String fullName, String email, String phoneNumber,
                String passwordHash, UserRole role, String drivingLicenseNumber,
                boolean isActive, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "User ID must not be null");
        this.fullName = Objects.requireNonNull(fullName, "Full name must not be null");
        this.email = Objects.requireNonNull(email, "Email must not be null").trim().toLowerCase();
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "Phone number must not be null");
        this.passwordHash = passwordHash;
        this.role = Objects.requireNonNull(role, "Role must not be null");
        this.drivingLicenseNumber = drivingLicenseNumber;
        this.isActive = isActive;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Constructs an active User account entity with default creation timestamp.
     */
    public User(String id, String fullName, String email, String phoneNumber,
                String passwordHash, UserRole role, String drivingLicenseNumber) {
        this(id, fullName, email, phoneNumber, passwordHash, role, drivingLicenseNumber, true, LocalDateTime.now());
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName, "Full name must not be null");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "Email must not be null").trim().toLowerCase();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "Phone number must not be null");
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = Objects.requireNonNull(role, "Role must not be null");
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isCustomer() {
        return this.role == UserRole.CUSTOMER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        // Redacts password hash to ensure security and prevent accidental log leakage
        return String.format("User [id=%s, name=%s, email=%s, role=%s, active=%s, dl=%s]",
                id, fullName, email, role, isActive,
                (drivingLicenseNumber != null ? drivingLicenseNumber : "N/A"));
    }
}

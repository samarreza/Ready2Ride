package com.ready2ride.core.model;

import java.time.LocalDateTime;

/**
 * Concrete model representing a Rental Provider (fleet owner/host) in Ready2Ride.
 * Role: {@link UserRole#RENTAL_PROVIDER}.
 */
public class RentalProvider extends User {

    public RentalProvider(String id, String fullName, String email, String phoneNumber,
                          String passwordHash, String drivingLicenseNumber) {
        super(id, fullName, email, phoneNumber, passwordHash, UserRole.RENTAL_PROVIDER, drivingLicenseNumber, true, LocalDateTime.now());
    }

    public RentalProvider(String id, String fullName, String email, String phoneNumber,
                          String passwordHash, String drivingLicenseNumber, boolean isActive, LocalDateTime createdAt) {
        super(id, fullName, email, phoneNumber, passwordHash, UserRole.RENTAL_PROVIDER, drivingLicenseNumber, isActive, createdAt);
    }
}

package com.ready2ride.core.model;

/**
 * Enumeration of possible booking lifecycle statuses.
 */
public enum BookingStatus {
    PENDING(true),
    CONFIRMED(true),
    CANCELLED(false),
    COMPLETED(false);

    private final boolean blocksAvailability;

    BookingStatus(boolean blocksAvailability) {
        this.blocksAvailability = blocksAvailability;
    }

    /**
     * Indicates whether a booking with this status occupies the vehicle schedule
     * and blocks new bookings for the same period.
     *
     * @return true if the booking blocks availability; false otherwise
     */
    public boolean blocksAvailability() {
        return blocksAvailability;
    }
}

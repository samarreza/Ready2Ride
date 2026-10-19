package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a vehicle rental booking reservation.
 */
public class Booking {

    private final String bookingId;
    private final String customerId;
    private final String vehicleId;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private BigDecimal rentalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking(String bookingId, String customerId, String vehicleId,
                   LocalDateTime pickupDateTime, LocalDateTime returnDateTime,
                   BigDecimal rentalAmount, BookingStatus status) {
        this(bookingId, customerId, vehicleId, pickupDateTime, returnDateTime, rentalAmount, status, LocalDateTime.now());
    }

    public Booking(String bookingId, String customerId, String vehicleId,
                   LocalDateTime pickupDateTime, LocalDateTime returnDateTime,
                   BigDecimal rentalAmount, BookingStatus status, LocalDateTime createdAt) {
        this.bookingId = Objects.requireNonNull(bookingId, "Booking ID must not be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID must not be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        this.pickupDateTime = Objects.requireNonNull(pickupDateTime, "Pickup date/time must not be null");
        this.returnDateTime = Objects.requireNonNull(returnDateTime, "Return date/time must not be null");
        this.rentalAmount = Objects.requireNonNull(rentalAmount, "Rental amount must not be null");
        this.status = Objects.requireNonNull(status, "Booking status must not be null");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Checks if this booking overlaps with a requested pickup/return window using standard interval logic:
     * {@code requestedPickup < existingReturn AND requestedReturn > existingPickup}
     *
     * @param requestedPickup requested start date/time
     * @param requestedReturn requested end date/time
     * @return true if overlapping; false otherwise
     */
    public boolean overlapsWith(LocalDateTime requestedPickup, LocalDateTime requestedReturn) {
        if (requestedPickup == null || requestedReturn == null) {
            return false;
        }
        return requestedPickup.isBefore(this.returnDateTime) && requestedReturn.isAfter(this.pickupDateTime);
    }

    /**
     * Checks if this booking is currently in a state that blocks vehicle availability.
     *
     * @return true if the booking blocks vehicle availability
     */
    public boolean blocksAvailability() {
        return this.status != null && this.status.blocksAvailability();
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = Objects.requireNonNull(pickupDateTime, "Pickup date/time must not be null");
    }

    public LocalDateTime getReturnDateTime() {
        return returnDateTime;
    }

    public void setReturnDateTime(LocalDateTime returnDateTime) {
        this.returnDateTime = Objects.requireNonNull(returnDateTime, "Return date/time must not be null");
    }

    public BigDecimal getRentalAmount() {
        return rentalAmount;
    }

    public void setRentalAmount(BigDecimal rentalAmount) {
        this.rentalAmount = Objects.requireNonNull(rentalAmount, "Rental amount must not be null");
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = Objects.requireNonNull(status, "Booking status must not be null");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking other)) return false;
        return Objects.equals(bookingId, other.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        return String.format("Booking [id=%s, customer=%s, vehicle=%s, pickup=%s, return=%s, amount=%s, status=%s]",
                bookingId, customerId, vehicleId, pickupDateTime, returnDateTime, rentalAmount, status);
    }
}

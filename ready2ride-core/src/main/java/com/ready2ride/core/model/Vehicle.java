package com.ready2ride.core.model;

import com.ready2ride.core.exception.InvalidRentalDateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing a rental vehicle.
 * Encapsulates common attributes and defines contract for polymorphic rental cost calculation.
 */
public abstract class Vehicle {

    private final String id;
    private String brand;
    private String model;
    private String registrationNumber;
    private BigDecimal rentalRate;
    private boolean isActive = true;

    /**
     * Constructs a new Vehicle with required core attributes.
     *
     * @param id                 unique vehicle identifier
     * @param brand              vehicle manufacturer brand
     * @param model              vehicle model name
     * @param registrationNumber legal registration plate number
     * @param rentalRate         configured base rental rate (hourly for bikes, daily for cars/SUVs/vans)
     */
    protected Vehicle(String id, String brand, String model, String registrationNumber, BigDecimal rentalRate) {
        this.id = Objects.requireNonNull(id, "Vehicle ID must not be null");
        this.brand = Objects.requireNonNull(brand, "Brand must not be null");
        this.model = Objects.requireNonNull(model, "Model must not be null");
        this.registrationNumber = Objects.requireNonNull(registrationNumber, "Registration number must not be null");
        setRentalRate(rentalRate);
    }

    /**
     * Calculates the rental cost for the given pickup and return date/time window.
     * Concrete implementations apply their respective rate policies polymorphically.
     *
     * @param pickupDateTime scheduled pickup date and time
     * @param returnDateTime scheduled return date and time
     * @return calculated rental cost as a {@link BigDecimal}
     * @throws InvalidRentalDateException if dates are null or returnDateTime is not strictly after pickupDateTime
     */
    public abstract BigDecimal calculateRentalCost(LocalDateTime pickupDateTime, LocalDateTime returnDateTime);

    /**
     * Returns the vehicle category type.
     *
     * @return {@link VehicleType} classification
     */
    public abstract VehicleType getVehicleType();

    /**
     * Validates that rental dates are non-null and return date/time is strictly after pickup date/time.
     *
     * @param pickupDateTime scheduled pickup date and time
     * @param returnDateTime scheduled return date and time
     * @throws InvalidRentalDateException if dates are invalid
     */
    protected void validateRentalDates(LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        if (pickupDateTime == null || returnDateTime == null) {
            throw new InvalidRentalDateException("Pickup date/time and return date/time must not be null.");
        }
        if (!returnDateTime.isAfter(pickupDateTime)) {
            throw new InvalidRentalDateException(String.format(
                    "Return date/time (%s) must be strictly later than pickup date/time (%s).",
                    returnDateTime, pickupDateTime));
        }
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = Objects.requireNonNull(brand, "Brand must not be null");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = Objects.requireNonNull(model, "Model must not be null");
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = Objects.requireNonNull(registrationNumber, "Registration number must not be null");
    }

    public BigDecimal getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        Objects.requireNonNull(rentalRate, "Rental rate must not be null");
        if (rentalRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rental rate cannot be negative.");
        }
        this.rentalRate = rentalRate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s [id=%s, brand=%s, model=%s, reg=%s, rate=%s]",
                getClass().getSimpleName(), id, brand, model, registrationNumber, rentalRate);
    }
}

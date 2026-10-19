package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a passenger or cargo van rental vehicle.
 * <p>
 * <b>Pricing Policy:</b> Vans are charged per started 24-hour period with a minimum
 * of one billable day for any valid rental window. The daily rate is configurable per van.
 * </p>
 */
public class Van extends Vehicle {

    private double cargoCapacityKg;

    public Van(String id, String brand, String model, String registrationNumber,
               BigDecimal dailyRentalRate, double cargoCapacityKg) {
        super(id, brand, model, registrationNumber, dailyRentalRate);
        this.cargoCapacityKg = cargoCapacityKg;
    }

    public Van(String id, String brand, String model, String registrationNumber, BigDecimal dailyRentalRate) {
        this(id, brand, model, registrationNumber, dailyRentalRate, 1200.0);
    }

    @Override
    public BigDecimal calculateRentalCost(LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        validateRentalDates(pickupDateTime, returnDateTime);

        Duration duration = Duration.between(pickupDateTime, returnDateTime);
        long totalMinutes = duration.toMinutes();

        // Charged per started 24-hour period (1440 minutes per day)
        long billableDays = (totalMinutes + 1439) / 1440;
        if (billableDays < 1) {
            billableDays = 1;
        }

        return getRentalRate()
                .multiply(BigDecimal.valueOf(billableDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.VAN;
    }

    public double getCargoCapacityKg() {
        return cargoCapacityKg;
    }

    public void setCargoCapacityKg(double cargoCapacityKg) {
        this.cargoCapacityKg = cargoCapacityKg;
    }
}

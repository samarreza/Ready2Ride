package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a standard passenger car rental vehicle (e.g. sedan, hatchback).
 * <p>
 * <b>Pricing Policy:</b> Cars are charged per started 24-hour period with a minimum
 * of one billable day for any valid rental window. The daily rate is configurable per car.
 * </p>
 */
public class Car extends Vehicle {

    private int seatingCapacity;
    private String fuelType;

    public Car(String id, String brand, String model, String registrationNumber,
               BigDecimal dailyRentalRate, int seatingCapacity, String fuelType) {
        super(id, brand, model, registrationNumber, dailyRentalRate);
        this.seatingCapacity = seatingCapacity;
        this.fuelType = fuelType;
    }

    public Car(String id, String brand, String model, String registrationNumber, BigDecimal dailyRentalRate) {
        this(id, brand, model, registrationNumber, dailyRentalRate, 5, "Petrol");
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
        return VehicleType.CAR;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
}

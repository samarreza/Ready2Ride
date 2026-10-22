package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a two-wheeler motorcycle or scooter rental vehicle.
 * <p>
 * <b>Pricing Policy:</b> Bikes are charged per started hour with a minimum of
 * one billable hour for any valid rental window. The hourly rate is configurable per bike.
 * </p>
 */
public class Bike extends Vehicle {

    private int engineCapacityCc;
    private int mileageKmpl = 45;
    private int seatingCapacity = 2;
    private String fuelType = "Petrol";

    public Bike(String id, String brand, String model, String registrationNumber,
                BigDecimal hourlyRentalRate, int engineCapacityCc, int mileageKmpl) {
        super(id, brand, model, registrationNumber, hourlyRentalRate);
        this.engineCapacityCc = engineCapacityCc;
        this.mileageKmpl = mileageKmpl;
    }

    public Bike(String id, String brand, String model, String registrationNumber,
                BigDecimal hourlyRentalRate, int engineCapacityCc) {
        this(id, brand, model, registrationNumber, hourlyRentalRate, engineCapacityCc, 45);
    }

    public Bike(String id, String brand, String model, String registrationNumber, BigDecimal hourlyRentalRate) {
        this(id, brand, model, registrationNumber, hourlyRentalRate, 150, 45);
    }

    @Override
    public BigDecimal calculateRentalCost(LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        validateRentalDates(pickupDateTime, returnDateTime);

        Duration duration = Duration.between(pickupDateTime, returnDateTime);
        long totalMinutes = duration.toMinutes();

        // Charged per started hour: e.g. 1-60 min = 1 hr, 61-120 min = 2 hrs
        long billableHours = (totalMinutes + 59) / 60;
        if (billableHours < 1) {
            billableHours = 1;
        }

        return getRentalRate()
                .multiply(BigDecimal.valueOf(billableHours))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.BIKE;
    }

    public int getEngineCapacityCc() {
        return engineCapacityCc;
    }

    public void setEngineCapacityCc(int engineCapacityCc) {
        this.engineCapacityCc = engineCapacityCc;
    }

    public int getMileageKmpl() {
        return mileageKmpl;
    }

    public void setMileageKmpl(int mileageKmpl) {
        this.mileageKmpl = mileageKmpl;
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

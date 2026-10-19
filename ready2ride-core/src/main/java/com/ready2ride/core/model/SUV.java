package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a Sport Utility Vehicle (SUV) rental vehicle.
 * <p>
 * <b>Pricing Policy:</b> SUVs are charged per started 24-hour period with a minimum
 * of one billable day for any valid rental window. The daily rate is configurable per SUV.
 * </p>
 */
public class SUV extends Vehicle {

    private boolean fourWheelDrive;
    private int seatingCapacity;

    public SUV(String id, String brand, String model, String registrationNumber,
               BigDecimal dailyRentalRate, boolean fourWheelDrive, int seatingCapacity) {
        super(id, brand, model, registrationNumber, dailyRentalRate);
        this.fourWheelDrive = fourWheelDrive;
        this.seatingCapacity = seatingCapacity;
    }

    public SUV(String id, String brand, String model, String registrationNumber, BigDecimal dailyRentalRate) {
        this(id, brand, model, registrationNumber, dailyRentalRate, true, 7);
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
        return VehicleType.SUV;
    }

    public boolean isFourWheelDrive() {
        return fourWheelDrive;
    }

    public void setFourWheelDrive(boolean fourWheelDrive) {
        this.fourWheelDrive = fourWheelDrive;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
}

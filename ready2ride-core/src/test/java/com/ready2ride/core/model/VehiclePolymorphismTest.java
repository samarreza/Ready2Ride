package com.ready2ride.core.model;

import com.ready2ride.core.exception.InvalidRentalDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehiclePolymorphismTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 5, 1, 10, 0);

    @Test
    @DisplayName("Should demonstrate polymorphism by holding subclasses as Vehicle references")
    void testVehiclePolymorphicReference() {
        Vehicle car = new Car("C1", "Toyota", "Camry", "MH-01-A-1111", new BigDecimal("50.00"));
        Vehicle bike = new Bike("B1", "Yamaha", "MT-15", "MH-01-B-2222", new BigDecimal("12.00"));
        Vehicle suv = new SUV("S1", "Ford", "Explorer", "MH-01-S-3333", new BigDecimal("80.00"));
        Vehicle van = new Van("V1", "Mercedes", "Sprinter", "MH-01-V-4444", new BigDecimal("100.00"));

        List<Vehicle> fleet = List.of(car, bike, suv, van);

        assertEquals(4, fleet.size());
        for (Vehicle vehicle : fleet) {
            assertNotNull(vehicle.getId());
            assertNotNull(vehicle.getBrand());
            assertNotNull(vehicle.getVehicleType());
        }
    }

    @Test
    @DisplayName("Bike pricing policy: charged per started hour, minimum 1 hour")
    void testBikePricingPolicy() {
        Vehicle bike = new Bike("B1", "Royal Enfield", "Hunter 350", "MH-02-B-1001", new BigDecimal("20.00"));

        // Exactly 30 minutes -> 1 started hour
        BigDecimal cost30Min = bike.calculateRentalCost(BASE_TIME, BASE_TIME.plusMinutes(30));
        assertEquals(new BigDecimal("20.00"), cost30Min);

        // Exactly 60 minutes -> 1 started hour
        BigDecimal cost60Min = bike.calculateRentalCost(BASE_TIME, BASE_TIME.plusMinutes(60));
        assertEquals(new BigDecimal("20.00"), cost60Min);

        // 61 minutes -> 2 started hours
        BigDecimal cost61Min = bike.calculateRentalCost(BASE_TIME, BASE_TIME.plusMinutes(61));
        assertEquals(new BigDecimal("40.00"), cost61Min);

        // 5 hours -> 5 started hours
        BigDecimal cost5Hours = bike.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(5));
        assertEquals(new BigDecimal("100.00"), cost5Hours);
    }

    @Test
    @DisplayName("Car pricing policy: charged per started 24-hour period, minimum 1 day")
    void testCarPricingPolicy() {
        Vehicle car = new Car("C1", "Honda", "Civic", "MH-02-C-2002", new BigDecimal("60.00"));

        // 2 hours -> 1 started day
        BigDecimal cost2Hours = car.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(2));
        assertEquals(new BigDecimal("60.00"), cost2Hours);

        // Exactly 24 hours -> 1 started day
        BigDecimal cost24Hours = car.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(24));
        assertEquals(new BigDecimal("60.00"), cost24Hours);

        // 24 hours and 1 minute -> 2 started days
        BigDecimal cost24Hours1Min = car.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(24).plusMinutes(1));
        assertEquals(new BigDecimal("120.00"), cost24Hours1Min);

        // 3 full days (72 hours) -> 3 started days
        BigDecimal cost3Days = car.calculateRentalCost(BASE_TIME, BASE_TIME.plusDays(3));
        assertEquals(new BigDecimal("180.00"), cost3Days);
    }

    @Test
    @DisplayName("SUV pricing policy: charged per started 24-hour period, minimum 1 day")
    void testSUVPricingPolicy() {
        Vehicle suv = new SUV("S1", "Toyota", "Fortuner", "MH-02-S-3003", new BigDecimal("95.00"));

        // 12 hours -> 1 started day
        BigDecimal cost12Hours = suv.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(12));
        assertEquals(new BigDecimal("95.00"), cost12Hours);

        // 48 hours -> 2 started days
        BigDecimal cost48Hours = suv.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(48));
        assertEquals(new BigDecimal("190.00"), cost48Hours);

        // 48 hours and 30 minutes -> 3 started days
        BigDecimal cost49Hours = suv.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(48).plusMinutes(30));
        assertEquals(new BigDecimal("285.00"), cost49Hours);
    }

    @Test
    @DisplayName("Van pricing policy: charged per started 24-hour period, minimum 1 day")
    void testVanPricingPolicy() {
        Vehicle van = new Van("V1", "Ford", "Transit", "MH-02-V-4004", new BigDecimal("120.00"));

        // 4 hours -> 1 started day
        BigDecimal cost4Hours = van.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(4));
        assertEquals(new BigDecimal("120.00"), cost4Hours);

        // 24 hours -> 1 started day
        BigDecimal cost24Hours = van.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(24));
        assertEquals(new BigDecimal("120.00"), cost24Hours);

        // 25 hours -> 2 started days
        BigDecimal cost25Hours = van.calculateRentalCost(BASE_TIME, BASE_TIME.plusHours(25));
        assertEquals(new BigDecimal("240.00"), cost25Hours);
    }

    @Test
    @DisplayName("Configurable rates per vehicle instance")
    void testConfigurableRates() {
        Vehicle budgetCar = new Car("C-BUDGET", "Maruti", "Swift", "DL-01-A-1111", new BigDecimal("35.00"));
        Vehicle luxuryCar = new Car("C-LUXURY", "BMW", "3 Series", "DL-01-A-9999", new BigDecimal("150.00"));

        BigDecimal budgetCost = budgetCar.calculateRentalCost(BASE_TIME, BASE_TIME.plusDays(2));
        BigDecimal luxuryCost = luxuryCar.calculateRentalCost(BASE_TIME, BASE_TIME.plusDays(2));

        assertEquals(new BigDecimal("70.00"), budgetCost);
        assertEquals(new BigDecimal("300.00"), luxuryCost);
    }

    @Test
    @DisplayName("Invalid rental dates should trigger InvalidRentalDateException")
    void testInvalidDatesTriggerException() {
        Vehicle car = new Car("C1", "Honda", "City", "MH-01-C-1234", new BigDecimal("50.00"));

        // Return before pickup
        assertThrows(InvalidRentalDateException.class, () ->
                car.calculateRentalCost(BASE_TIME, BASE_TIME.minusHours(1)));

        // Return same as pickup
        assertThrows(InvalidRentalDateException.class, () ->
                car.calculateRentalCost(BASE_TIME, BASE_TIME));

        // Null dates
        assertThrows(InvalidRentalDateException.class, () ->
                car.calculateRentalCost(null, BASE_TIME.plusDays(1)));
        assertThrows(InvalidRentalDateException.class, () ->
                car.calculateRentalCost(BASE_TIME, null));
    }
}

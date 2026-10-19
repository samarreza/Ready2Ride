package com.ready2ride.core.repository;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {

    private InMemoryVehicleRepository vehicleRepository;
    private InMemoryBookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository = new InMemoryVehicleRepository();
        bookingRepository = new InMemoryBookingRepository();
    }

    @Test
    @DisplayName("Vehicle repository: store, retrieve, check existence, and delete via HashMap")
    void testVehicleRepositoryOperations() {
        Vehicle car = new Car("V-CAR-1", "Honda", "Elevate", "MH-14-AA-1000", new BigDecimal("55.00"));
        Vehicle bike = new Bike("V-BIKE-1", "KTM", "Duke 250", "MH-14-BB-2000", new BigDecimal("18.00"));

        vehicleRepository.save(car);
        vehicleRepository.save(bike);

        // Retrieve by ID
        Optional<Vehicle> foundCar = vehicleRepository.findById("V-CAR-1");
        assertTrue(foundCar.isPresent());
        assertEquals("Honda", foundCar.get().getBrand());

        // Check existence
        assertTrue(vehicleRepository.existsById("V-BIKE-1"));
        assertFalse(vehicleRepository.existsById("V-UNKNOWN"));

        // Find all
        List<Vehicle> all = vehicleRepository.findAll();
        assertEquals(2, all.size());

        // Delete
        assertTrue(vehicleRepository.deleteById("V-CAR-1"));
        assertFalse(vehicleRepository.existsById("V-CAR-1"));
        assertEquals(1, vehicleRepository.findAll().size());
    }

    @Test
    @DisplayName("Booking repository: store, retrieve by ID, find by vehicle, and list via ArrayList")
    void testBookingRepositoryOperations() {
        LocalDateTime pickup = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime dropoff = LocalDateTime.of(2026, 6, 3, 9, 0);

        Booking booking1 = new Booking(
                "BKG-100", "CUST-01", "VEH-01",
                pickup, dropoff, new BigDecimal("110.00"), BookingStatus.CONFIRMED);

        Booking booking2 = new Booking(
                "BKG-200", "CUST-02", "VEH-01",
                dropoff.plusDays(1), dropoff.plusDays(2), new BigDecimal("55.00"), BookingStatus.PENDING);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        // Retrieve by ID
        Optional<Booking> retrieved = bookingRepository.findById("BKG-100");
        assertTrue(retrieved.isPresent());
        assertEquals("CUST-01", retrieved.get().getCustomerId());
        assertEquals(new BigDecimal("110.00"), retrieved.get().getRentalAmount());

        // Find by vehicle ID
        List<Booking> byVehicle = bookingRepository.findByVehicleId("VEH-01");
        assertEquals(2, byVehicle.size());

        // Find by customer ID
        List<Booking> byCustomer = bookingRepository.findByCustomerId("CUST-02");
        assertEquals(1, byCustomer.size());
        assertEquals("BKG-200", byCustomer.get(0).getBookingId());

        // Update booking (save replaces existing with same bookingId)
        booking1.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking1);
        assertEquals(BookingStatus.CANCELLED, bookingRepository.findById("BKG-100").get().getStatus());
        assertEquals(2, bookingRepository.findAll().size());

        // Delete
        assertTrue(bookingRepository.deleteById("BKG-200"));
        assertFalse(bookingRepository.findById("BKG-200").isPresent());
        assertEquals(1, bookingRepository.findAll().size());
    }
}

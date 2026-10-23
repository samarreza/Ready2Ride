package com.ready2ride.core.service;

import com.ready2ride.core.exception.BookingNotFoundException;
import com.ready2ride.core.exception.InvalidRentalDateException;
import com.ready2ride.core.exception.VehicleNotAvailableException;
import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.InMemoryBookingRepository;
import com.ready2ride.core.repository.InMemoryVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingServiceTest {

    private InMemoryVehicleRepository vehicleRepository;
    private InMemoryBookingRepository bookingRepository;
    private BookingService bookingService;
    private VehicleService vehicleService;

    private static final LocalDateTime START_DATE = LocalDateTime.of(2026, 7, 10, 10, 0);
    private static final LocalDateTime END_DATE_2DAYS = LocalDateTime.of(2026, 7, 12, 10, 0);

    @BeforeEach
    void setUp() {
        vehicleRepository = new InMemoryVehicleRepository();
        bookingRepository = new InMemoryBookingRepository();
        bookingService = new BookingService(bookingRepository, vehicleRepository);
        vehicleService = new VehicleService(vehicleRepository);

        // Register sample fleet across all subclasses
        Vehicle car = new Car("CAR-1", "Hyundai", "Verna", "DL-01-C-1001", new BigDecimal("50.00"));
        Vehicle bike = new Bike("BIKE-1", "Honda", "CB350", "DL-01-B-2002", new BigDecimal("15.00"));
        Vehicle suv = new SUV("SUV-1", "Mahindra", "XUV700", "DL-01-S-3003", new BigDecimal("80.00"));
        Vehicle van = new Van("VAN-1", "Toyota", "HiAce", "DL-01-V-4004", new BigDecimal("110.00"));

        vehicleRepository.save(car);
        vehicleRepository.save(bike);
        vehicleRepository.save(suv);
        vehicleRepository.save(van);
    }

    @Test
    @DisplayName("Should create booking with polymorphic cost calculation for different vehicle types")
    void testCreateBookingPolymorphicCalculation() {
        // Car: 2 full days @ 50.00/day = 100.00
        Booking carBooking = bookingService.createBooking("BKG-CAR", "CUST-1", "CAR-1",
                START_DATE, END_DATE_2DAYS);
        assertEquals(new BigDecimal("100.00"), carBooking.getRentalAmount());

        // Bike: 3 hours @ 15.00/hr = 45.00
        Booking bikeBooking = bookingService.createBooking("BKG-BIKE", "CUST-2", "BIKE-1",
                START_DATE, START_DATE.plusHours(3));
        assertEquals(new BigDecimal("45.00"), bikeBooking.getRentalAmount());

        // SUV: 1 day @ 80.00/day = 80.00
        Booking suvBooking = bookingService.createBooking("BKG-SUV", "CUST-3", "SUV-1",
                START_DATE, START_DATE.plusHours(18));
        assertEquals(new BigDecimal("80.00"), suvBooking.getRentalAmount());

        // Van: 2 days (26 hours = 2 started days) @ 110.00/day = 220.00
        Booking vanBooking = bookingService.createBooking("BKG-VAN", "CUST-4", "VAN-1",
                START_DATE, START_DATE.plusHours(26));
        assertEquals(new BigDecimal("220.00"), vanBooking.getRentalAmount());
    }

    @Test
    @DisplayName("Invalid rental dates should trigger InvalidRentalDateException")
    void testInvalidRentalDatesTriggerException() {
        // Return before pickup
        assertThrows(InvalidRentalDateException.class, () ->
                bookingService.createBooking("CUST-1", "CAR-1", START_DATE, START_DATE.minusDays(1)));

        // Return same as pickup
        assertThrows(InvalidRentalDateException.class, () ->
                bookingService.createBooking("CUST-1", "CAR-1", START_DATE, START_DATE));

        // Null dates
        assertThrows(InvalidRentalDateException.class, () ->
                bookingService.createBooking("CUST-1", "CAR-1", null, END_DATE_2DAYS));
        assertThrows(InvalidRentalDateException.class, () ->
                bookingService.createBooking("CUST-1", "CAR-1", START_DATE, null));
    }

    @Test
    @DisplayName("Overlapping reservation should trigger VehicleNotAvailableException")
    void testOverlappingReservationTriggersException() {
        // Existing booking: July 10, 10:00 -> July 12, 10:00
        bookingService.createBooking("BKG-ORIG", "CUST-1", "CAR-1", START_DATE, END_DATE_2DAYS);

        // Case 1: Identical interval
        assertThrows(VehicleNotAvailableException.class, () ->
                bookingService.createBooking("CUST-2", "CAR-1", START_DATE, END_DATE_2DAYS));

        // Case 2: Overlapping start (July 9, 10:00 -> July 11, 10:00)
        assertThrows(VehicleNotAvailableException.class, () ->
                bookingService.createBooking("CUST-2", "CAR-1", START_DATE.minusDays(1), START_DATE.plusDays(1)));

        // Case 3: Overlapping end (July 11, 10:00 -> July 13, 10:00)
        assertThrows(VehicleNotAvailableException.class, () ->
                bookingService.createBooking("CUST-2", "CAR-1", START_DATE.plusDays(1), END_DATE_2DAYS.plusDays(1)));

        // Case 4: Enclosing interval (July 9, 10:00 -> July 13, 10:00)
        assertThrows(VehicleNotAvailableException.class, () ->
                bookingService.createBooking("CUST-2", "CAR-1", START_DATE.minusDays(1), END_DATE_2DAYS.plusDays(1)));

        // Case 5: Contained inside interval (July 10, 12:00 -> July 11, 12:00)
        assertThrows(VehicleNotAvailableException.class, () ->
                bookingService.createBooking("CUST-2", "CAR-1", START_DATE.plusHours(2), START_DATE.plusDays(1)));
    }

    @Test
    @DisplayName("Non-overlapping contiguous bookings should succeed")
    void testNonOverlappingContiguousBookingsSucceed() {
        // First booking: July 10, 10:00 -> July 12, 10:00
        bookingService.createBooking("BKG-1", "CUST-1", "CAR-1", START_DATE, END_DATE_2DAYS);

        // Subsequent booking starts exactly when first ends: July 12, 10:00 -> July 14, 10:00
        Booking booking2 = bookingService.createBooking("BKG-2", "CUST-2", "CAR-1",
                END_DATE_2DAYS, END_DATE_2DAYS.plusDays(2));
        assertNotNull(booking2);
        assertEquals("BKG-2", booking2.getBookingId());

        // Preceding booking ends exactly when first starts: July 8, 10:00 -> July 10, 10:00
        Booking booking0 = bookingService.createBooking("BKG-0", "CUST-0", "CAR-1",
                START_DATE.minusDays(2), START_DATE);
        assertNotNull(booking0);
        assertEquals("BKG-0", booking0.getBookingId());
    }

    @Test
    @DisplayName("Booking can be stored and retrieved through repository via service")
    void testBookingStorageAndRetrieval() {
        Booking created = bookingService.createBooking("BKG-TEST", "CUST-10", "BIKE-1",
                START_DATE, START_DATE.plusHours(2));

        Booking retrieved = bookingService.getBookingById("BKG-TEST");
        assertEquals(created.getBookingId(), retrieved.getBookingId());
        assertEquals("CUST-10", retrieved.getCustomerId());
        assertEquals(BookingStatus.CONFIRMED, retrieved.getStatus());
    }

    @Test
    @DisplayName("Request for non-existent booking should trigger BookingNotFoundException")
    void testMissingBookingTriggersException() {
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.getBookingById("NON-EXISTENT-BKG"));

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.cancelBooking("NON-EXISTENT-BKG"));
    }

    @Test
    @DisplayName("Request for non-existent vehicle should trigger VehicleNotFoundException")
    void testMissingVehicleTriggersException() {
        assertThrows(VehicleNotFoundException.class, () ->
                bookingService.createBooking("CUST-1", "NO-SUCH-VEHICLE", START_DATE, END_DATE_2DAYS));
    }

    @Test
    @DisplayName("Cancelled booking should no longer block its former time range")
    void testCancelledBookingUnblocksAvailability() {
        // Create initial booking
        Booking original = bookingService.createBooking("BKG-TO-CANCEL", "CUST-1", "CAR-1",
                START_DATE, END_DATE_2DAYS);
        assertEquals(BookingStatus.CONFIRMED, original.getStatus());

        // Vehicle is initially not available for that period
        assertFalse(bookingService.isVehicleAvailable("CAR-1", START_DATE, END_DATE_2DAYS));

        // Cancel the booking
        Booking cancelled = bookingService.cancelBooking("BKG-TO-CANCEL");
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        // Vehicle should now be available again for the exact same timeframe
        assertTrue(bookingService.isVehicleAvailable("CAR-1", START_DATE, END_DATE_2DAYS));

        // Another customer should now be able to book the exact same timeframe without exception
        Booking newBooking = bookingService.createBooking("BKG-NEW", "CUST-2", "CAR-1",
                START_DATE, END_DATE_2DAYS);
        assertNotNull(newBooking);
        assertEquals("BKG-NEW", newBooking.getBookingId());
        assertEquals(BookingStatus.CONFIRMED, newBooking.getStatus());
    }

    @Test
    @DisplayName("Customer ownership check should prevent unauthorized access and cancellation")
    void testCustomerOwnershipEnforcement() {
        Booking booking = bookingService.createBooking("BKG-OWNED", "CUST-ALICE", "CAR-1",
                START_DATE.plusDays(10), END_DATE_2DAYS.plusDays(10));
        assertEquals("CUST-ALICE", booking.getCustomerId());

        // Alice can access her own booking
        Booking retrieved = bookingService.getBookingForCustomer("BKG-OWNED", "CUST-ALICE");
        assertNotNull(retrieved);

        // Bob cannot access Alice's booking
        assertThrows(SecurityException.class, () ->
                bookingService.getBookingForCustomer("BKG-OWNED", "CUST-BOB"));

        // Bob cannot cancel Alice's booking
        assertThrows(SecurityException.class, () ->
                bookingService.cancelBookingForCustomer("BKG-OWNED", "CUST-BOB"));

        // Alice can successfully cancel her own booking
        Booking cancelled = bookingService.cancelBookingForCustomer("BKG-OWNED", "CUST-ALICE");
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }
}


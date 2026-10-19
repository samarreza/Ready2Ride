package com.ready2ride.core.repository;

import com.ready2ride.core.exception.VehicleNotAvailableException;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.MaintenanceRecord;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.PaymentMethod;
import com.ready2ride.core.model.PaymentStatus;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentAndMaintenanceRepositoryTest {

    private InMemoryPaymentRepository paymentRepository;
    private InMemoryMaintenanceRepository maintenanceRepository;
    private InMemoryVehicleRepository vehicleRepository;
    private InMemoryBookingRepository bookingRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        paymentRepository = new InMemoryPaymentRepository();
        maintenanceRepository = new InMemoryMaintenanceRepository();
        vehicleRepository = new InMemoryVehicleRepository();
        bookingRepository = new InMemoryBookingRepository();
        bookingService = new BookingService(bookingRepository, vehicleRepository, maintenanceRepository);
    }

    @Test
    @DisplayName("1. Payment persistence, lookup by ID and booking ID")
    void testPaymentPersistence() {
        Payment payment = new Payment(
                "PAY-001", "BKG-101", new BigDecimal("120.00"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                PaymentMethod.UPI, PaymentStatus.COMPLETED, "UPI-REF-998877"
        );

        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findById("PAY-001");
        assertTrue(found.isPresent());
        assertEquals("BKG-101", found.get().getBookingId());
        assertEquals(new BigDecimal("120.00"), found.get().getAmount());
        assertEquals("UPI-REF-998877", found.get().getTransactionReference());
        assertEquals(PaymentMethod.UPI, found.get().getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, found.get().getStatus());

        List<Payment> byBooking = paymentRepository.findByBookingId("BKG-101");
        assertEquals(1, byBooking.size());
        assertEquals("PAY-001", byBooking.get(0).getPaymentId());

        assertTrue(paymentRepository.deleteById("PAY-001"));
        assertTrue(paymentRepository.findById("PAY-001").isEmpty());
    }

    @Test
    @DisplayName("2. Maintenance record persistence and active maintenance window detection")
    void testMaintenancePersistence() {
        LocalDate serviceDate = LocalDate.of(2026, 11, 5);
        MaintenanceRecord record = new MaintenanceRecord(
                "MNT-001", "V-CAR-01", serviceDate,
                "Brake fluid replacement and rotor turning", new BigDecimal("150.00"), false
        );

        maintenanceRepository.save(record);

        Optional<MaintenanceRecord> found = maintenanceRepository.findById("MNT-001");
        assertTrue(found.isPresent());
        assertEquals("V-CAR-01", found.get().getVehicleId());
        assertFalse(found.get().isCompleted());

        // Check window containing maintenance date
        assertTrue(maintenanceRepository.hasActiveMaintenance("V-CAR-01",
                LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 6)));

        // Check window completely outside maintenance date
        assertFalse(maintenanceRepository.hasActiveMaintenance("V-CAR-01",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)));

        // Mark as completed - should no longer block
        record.setCompleted(true);
        maintenanceRepository.save(record);
        assertFalse(maintenanceRepository.hasActiveMaintenance("V-CAR-01",
                LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 6)));
    }

    @Test
    @DisplayName("3. BookingService rejects booking if vehicle is under scheduled maintenance")
    void testBookingServiceRejectsMaintenanceConflict() {
        Vehicle car = new Car("V-CAR-02", "Honda", "City", "MH-12-AB-1234", new BigDecimal("50.00"));
        vehicleRepository.save(car);

        // Schedule incomplete maintenance on Nov 5
        MaintenanceRecord mnt = new MaintenanceRecord(
                "MNT-002", "V-CAR-02", LocalDate.of(2026, 11, 5),
                "Transmission repair", new BigDecimal("300.00"), false
        );
        maintenanceRepository.save(mnt);

        // Attempt booking that covers Nov 4 to Nov 6
        LocalDateTime pickup = LocalDateTime.of(2026, 11, 4, 10, 0);
        LocalDateTime returnTime = LocalDateTime.of(2026, 11, 6, 10, 0);

        assertThrows(VehicleNotAvailableException.class,
                () -> bookingService.createBooking("CUST-1", "V-CAR-02", pickup, returnTime));

        assertFalse(bookingService.isVehicleAvailable("V-CAR-02", pickup, returnTime));
    }

    @Test
    @DisplayName("4. BookingService rejects booking if vehicle is deactivated (inactive)")
    void testBookingServiceRejectsInactiveVehicle() {
        Vehicle car = new Car("V-CAR-INACTIVE", "Hyundai", "Verna", "MH-12-AB-9999", new BigDecimal("50.00"));
        car.setActive(false);
        vehicleRepository.save(car);

        LocalDateTime pickup = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime returnTime = LocalDateTime.of(2026, 11, 12, 10, 0);

        assertThrows(VehicleNotAvailableException.class,
                () -> bookingService.createBooking("CUST-1", "V-CAR-INACTIVE", pickup, returnTime));

        assertFalse(bookingService.isVehicleAvailable("V-CAR-INACTIVE", pickup, returnTime));
    }
}

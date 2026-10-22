package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConfig;
import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.database.DatabaseSchemaInitializer;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.exception.InvalidRentalDateException;
import com.ready2ride.core.exception.VehicleNotAvailableException;
import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.MaintenanceRecord;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.PaymentMethod;
import com.ready2ride.core.model.PaymentStatus;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.util.PasswordHasher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresIntegrationTest {

    private static DatabaseConnectionManager connectionManager;
    private static JdbcUserRepository userRepository;
    private static JdbcVehicleRepository vehicleRepository;
    private static JdbcBookingRepository bookingRepository;
    private static JdbcPaymentRepository paymentRepository;
    private static JdbcMaintenanceRepository maintenanceRepository;
    private static AuthenticationService authService;
    private static boolean databaseAvailable = false;

    private static final String TEST_CUSTOMER_ID = "CUST-IT-USER-1";
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2027, 1, 10, 10, 0);

    @BeforeAll
    static void setUpAll() {
        connectionManager = new DatabaseConnectionManager(DatabaseConfig.fromEnvironment());
        databaseAvailable = connectionManager.isDatabaseAvailable(2);

        if (databaseAvailable) {
            // Apply schema if needed
            DatabaseSchemaInitializer initializer = new DatabaseSchemaInitializer(connectionManager);
            Path schemaPath = Paths.get("..", "database", "schema.sql");
            if (!schemaPath.toFile().exists()) {
                schemaPath = Paths.get("database", "schema.sql");
            }
            if (schemaPath.toFile().exists()) {
                initializer.executeScriptFile(schemaPath);
            }

            Path migV6Path = Paths.get("..", "database", "migration_v5_to_v6_rental_provider.sql");
            if (!migV6Path.toFile().exists()) {
                migV6Path = Paths.get("database", "migration_v5_to_v6_rental_provider.sql");
            }
            if (migV6Path.toFile().exists()) {
                initializer.executeScriptFile(migV6Path);
            }

            // Clean up test data from any previous runs to ensure repeatable execution
            cleanupPreviousTestData();

            userRepository = new JdbcUserRepository(connectionManager);
            vehicleRepository = new JdbcVehicleRepository(connectionManager);
            bookingRepository = new JdbcBookingRepository(connectionManager);
            paymentRepository = new JdbcPaymentRepository(connectionManager);
            maintenanceRepository = new JdbcMaintenanceRepository(connectionManager);
            authService = new AuthenticationService(userRepository);

            // Ensure test customer exists in users table
            ensureTestUserExists(TEST_CUSTOMER_ID, "Integration Test Customer", "it.customer@example.com");
        }
    }

    private static void ensureTestUserExists(String id, String name, String email) {
        String sql = """
                INSERT INTO users (id, full_name, email, phone_number, password_hash, role, is_active)
                VALUES (?, ?, ?, '+1-555-0199', '$2a$12$devPlaceholderHashForTestingOnly001', 'CUSTOMER', TRUE)
                ON CONFLICT (id) DO UPDATE SET full_name = EXCLUDED.full_name
                """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to prepare test customer: " + e.getMessage(), e);
        }
    }

    private void checkDatabase() {
        Assumptions.assumeTrue(databaseAvailable,
                "PostgreSQL integration test skipped: Database is not accessible. " +
                "Configure READY2RIDE_DB_URL, READY2RIDE_DB_USER, and READY2RIDE_DB_PASSWORD to run.");
    }

    /**
     * Removes all test-specific rows (prefixed with "IT-" or "CUST-IT-") from the database.
     * Deletion order respects foreign key constraints: payments → bookings → maintenance → vehicles → users.
     * This ensures repeatable test runs against a persistent development database
     * without destroying seed or production data.
     */
    private static void cleanupPreviousTestData() {
        String[] cleanupStatements = {
            "DELETE FROM payments WHERE id LIKE 'IT-%'",
            "DELETE FROM bookings WHERE id LIKE 'IT-%'",
            "DELETE FROM maintenance WHERE id LIKE 'IT-%'",
            "DELETE FROM vehicles WHERE id LIKE 'IT-%'",
            "DELETE FROM users WHERE id LIKE 'IT-%' OR id LIKE 'CUST-IT-%'"
        };

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : cleanupStatements) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            System.err.println("[PostgresIntegrationTest] Warning: test data cleanup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Opening a valid PostgreSQL connection")
    void testOpenValidConnection() throws SQLException {
        checkDatabase();
        try (Connection conn = connectionManager.getConnection()) {
            assertNotNull(conn);
            assertTrue(conn.isValid(2));
            assertFalse(conn.isClosed());
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. Saving and retrieving vehicles polymorphically (Car, Bike, SUV, Van)")
    void testSaveAndRetrieveVehicles() {
        checkDatabase();

        Vehicle car = new Car("IT-CAR-1", "Honda", "Amaze", "IT-REG-CAR-1", new BigDecimal("45.00"), 5, "Petrol");
        Vehicle bike = new Bike("IT-BIKE-1", "TVS", "Apache", "IT-REG-BIKE-1", new BigDecimal("12.00"), 160);
        Vehicle suv = new SUV("IT-SUV-1", "Tata", "Safari", "IT-REG-SUV-1", new BigDecimal("75.00"), true, 7);
        Vehicle van = new Van("IT-VAN-1", "Force", "Urbania", "IT-REG-VAN-1", new BigDecimal("115.00"), 1400.0);

        vehicleRepository.save(car);
        vehicleRepository.save(bike);
        vehicleRepository.save(suv);
        vehicleRepository.save(van);

        Optional<Vehicle> retrievedCar = vehicleRepository.findById("IT-CAR-1");
        assertTrue(retrievedCar.isPresent());
        assertTrue(retrievedCar.get() instanceof Car);
        assertEquals("Honda", retrievedCar.get().getBrand());

        Optional<Vehicle> retrievedBike = vehicleRepository.findById("IT-BIKE-1");
        assertTrue(retrievedBike.isPresent());
        assertTrue(retrievedBike.get() instanceof Bike);

        Optional<Vehicle> retrievedSuv = vehicleRepository.findById("IT-SUV-1");
        assertTrue(retrievedSuv.isPresent());
        assertTrue(retrievedSuv.get() instanceof SUV);

        Optional<Vehicle> retrievedVan = vehicleRepository.findById("IT-VAN-1");
        assertTrue(retrievedVan.isPresent());
        assertTrue(retrievedVan.get() instanceof Van);
    }

    @Test
    @Order(3)
    @DisplayName("3. Updating vehicle details in PostgreSQL")
    void testUpdateVehicleDetails() {
        checkDatabase();

        Vehicle car = vehicleRepository.findById("IT-CAR-1").orElseThrow();
        car.setRentalRate(new BigDecimal("49.50"));
        car.setModel("Amaze Facelift");
        vehicleRepository.save(car);

        Vehicle updated = vehicleRepository.findById("IT-CAR-1").orElseThrow();
        assertEquals(new BigDecimal("49.50"), updated.getRentalRate());
        assertEquals("Amaze Facelift", updated.getModel());
    }

    @Test
    @Order(4)
    @DisplayName("4. Saving and retrieving bookings from PostgreSQL")
    void testSaveAndRetrieveBooking() {
        checkDatabase();

        Booking booking = new Booking(
                "IT-BKG-1", TEST_CUSTOMER_ID, "IT-CAR-1",
                BASE_TIME, BASE_TIME.plusDays(2), new BigDecimal("99.00"), BookingStatus.CONFIRMED
        );
        bookingRepository.save(booking);

        Optional<Booking> retrieved = bookingRepository.findById("IT-BKG-1");
        assertTrue(retrieved.isPresent());
        assertEquals(TEST_CUSTOMER_ID, retrieved.get().getCustomerId());
        assertEquals("IT-CAR-1", retrieved.get().getVehicleId());
        assertEquals(new BigDecimal("99.00"), retrieved.get().getRentalAmount());
        assertEquals(BookingStatus.CONFIRMED, retrieved.get().getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("5. Retrieving bookings by customer and vehicle")
    void testQueryBookingsByCustomerAndVehicle() {
        checkDatabase();

        List<Booking> byCustomer = bookingRepository.findByCustomerId(TEST_CUSTOMER_ID);
        assertFalse(byCustomer.isEmpty());
        assertTrue(byCustomer.stream().anyMatch(b -> "IT-BKG-1".equals(b.getBookingId())));

        List<Booking> byVehicle = bookingRepository.findByVehicleId("IT-CAR-1");
        assertFalse(byVehicle.isEmpty());
        assertTrue(byVehicle.stream().anyMatch(b -> "IT-BKG-1".equals(b.getBookingId())));
    }

    @Test
    @Order(6)
    @DisplayName("6. Handling missing IDs gracefully")
    void testHandlingMissingIds() {
        checkDatabase();

        Optional<Vehicle> missingVehicle = vehicleRepository.findById("NON-EXISTENT-V-ID");
        assertFalse(missingVehicle.isPresent());

        Optional<Booking> missingBooking = bookingRepository.findById("NON-EXISTENT-B-ID");
        assertFalse(missingBooking.isPresent());

        // Booking with non-existent vehicle throws VehicleNotFoundException
        Booking invalidVehicleBooking = new Booking(
                "IT-BKG-NO-V", TEST_CUSTOMER_ID, "NO-SUCH-VEHICLE",
                BASE_TIME.plusDays(10), BASE_TIME.plusDays(12), new BigDecimal("50.00"), BookingStatus.CONFIRMED
        );
        assertThrows(VehicleNotFoundException.class, () -> bookingRepository.save(invalidVehicleBooking));
    }

    @Test
    @Order(7)
    @DisplayName("7. Enforcing foreign-key and uniqueness constraints")
    void testEnforcingDatabaseConstraints() {
        checkDatabase();

        // Foreign key violation: customer ID does not exist in users table
        Booking badCustomerBooking = new Booking(
                "IT-BKG-BAD-CUST", "NON-EXISTENT-CUSTOMER-UUID", "IT-CAR-1",
                BASE_TIME.plusDays(20), BASE_TIME.plusDays(22), new BigDecimal("50.00"), BookingStatus.CONFIRMED
        );
        assertThrows(DataAccessException.class, () -> bookingRepository.save(badCustomerBooking));

        // Uniqueness constraint violation: duplicate registration plate number
        Vehicle duplicateRegVehicle = new Car(
                "IT-CAR-DUP", "Honda", "Elevate", "IT-REG-CAR-1", new BigDecimal("55.00"), 5, "Petrol"
        );
        assertThrows(DataAccessException.class, () -> vehicleRepository.save(duplicateRegVehicle));
    }

    @Test
    @Order(8)
    @DisplayName("8. Rejecting invalid date ranges")
    void testRejectInvalidDateRanges() {
        checkDatabase();

        // Return date before pickup
        Booking invertedDates = new Booking(
                "IT-BKG-INVERTED", TEST_CUSTOMER_ID, "IT-CAR-1",
                BASE_TIME.plusDays(2), BASE_TIME, new BigDecimal("50.00"), BookingStatus.CONFIRMED
        );
        assertThrows(InvalidRentalDateException.class, () -> bookingRepository.save(invertedDates));
    }

    @Test
    @Order(9)
    @DisplayName("9. Rejecting overlapping active bookings with VehicleNotAvailableException")
    void testRejectOverlappingActiveBookings() {
        checkDatabase();

        // IT-BKG-1 is currently booked for BASE_TIME to BASE_TIME + 2 days
        // Attempt an overlapping booking (starts 1 day in, ends 3 days in)
        Booking overlapping = new Booking(
                "IT-BKG-OVERLAP", TEST_CUSTOMER_ID, "IT-CAR-1",
                BASE_TIME.plusDays(1), BASE_TIME.plusDays(3), new BigDecimal("99.00"), BookingStatus.CONFIRMED
        );

        assertThrows(VehicleNotAvailableException.class, () -> bookingRepository.save(overlapping));
    }

    @Test
    @Order(10)
    @DisplayName("10. Allowing valid non-overlapping bookings")
    void testAllowNonOverlappingBookings() {
        checkDatabase();

        // IT-BKG-1 ends at BASE_TIME + 2 days. Subsequent booking starts exactly at BASE_TIME + 2 days
        Booking subsequent = new Booking(
                "IT-BKG-SUBSEQUENT", TEST_CUSTOMER_ID, "IT-CAR-1",
                BASE_TIME.plusDays(2), BASE_TIME.plusDays(4), new BigDecimal("99.00"), BookingStatus.CONFIRMED
        );

        Booking saved = bookingRepository.save(subsequent);
        assertNotNull(saved);
        assertEquals("IT-BKG-SUBSEQUENT", saved.getBookingId());
    }

    @Test
    @Order(11)
    @DisplayName("11. Ignoring cancelled bookings when checking availability")
    void testIgnoringCancelledBookings() {
        checkDatabase();

        // Book IT-BIKE-1
        LocalDateTime bikeStart = BASE_TIME.plusDays(5);
        LocalDateTime bikeEnd = bikeStart.plusHours(4);

        Booking bikeBooking = new Booking(
                "IT-BKG-BIKE-CANCEL", TEST_CUSTOMER_ID, "IT-BIKE-1",
                bikeStart, bikeEnd, new BigDecimal("48.00"), BookingStatus.CONFIRMED
        );
        bookingRepository.save(bikeBooking);

        // Cancel the booking
        bikeBooking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(bikeBooking);

        // Another customer books the EXACT same vehicle and timeframe
        Booking replacementBooking = new Booking(
                "IT-BKG-BIKE-REPLACEMENT", TEST_CUSTOMER_ID, "IT-BIKE-1",
                bikeStart, bikeEnd, new BigDecimal("48.00"), BookingStatus.CONFIRMED
        );

        Booking saved = bookingRepository.save(replacementBooking);
        assertNotNull(saved);
        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("12. Rolling back failed booking transactions")
    void testTransactionRollbackOnFailure() {
        checkDatabase();

        // An invalid booking attempt that triggers a constraint or collision
        String collisionBookingId = "IT-BKG-FAIL-ROLLBACK";
        Booking collision = new Booking(
                collisionBookingId, TEST_CUSTOMER_ID, "IT-CAR-1",
                BASE_TIME, BASE_TIME.plusDays(2), new BigDecimal("99.00"), BookingStatus.CONFIRMED
        );

        assertThrows(VehicleNotAvailableException.class, () -> bookingRepository.save(collision));

        // Verify that the record was not persisted due to transaction rollback
        Optional<Booking> check = bookingRepository.findById(collisionBookingId);
        assertFalse(check.isPresent());
    }

    @Test
    @Order(13)
    @DisplayName("13. Handling competing booking requests safely with concurrency locking")
    void testCompetingBookingRequestsSafety() throws InterruptedException {
        checkDatabase();

        // Vehicle IT-SUV-1 has no existing reservations for this window
        LocalDateTime compStart = BASE_TIME.plusDays(15);
        LocalDateTime compEnd = compStart.plusDays(3);

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to start simultaneously
                    Booking competingBooking = new Booking(
                            "IT-BKG-COMPETE-" + index + "-" + UUID.randomUUID().toString().substring(0, 4),
                            TEST_CUSTOMER_ID, "IT-SUV-1",
                            compStart, compEnd, new BigDecimal("225.00"), BookingStatus.CONFIRMED
                    );
                    bookingRepository.save(competingBooking);
                    successCount.incrementAndGet();
                } catch (VehicleNotAvailableException ex) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected error
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Fire both competing threads simultaneously
        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        // Exactly one competing thread should succeed and the other must be rejected
        assertEquals(1, successCount.get(), "Exactly one concurrent booking request must succeed");
        assertEquals(1, failureCount.get(), "The competing concurrent booking request must be rejected with VehicleNotAvailableException");
    }

    @Test
    @Order(14)
    @DisplayName("14. User creation, persistent storage and normalized lookup in PostgreSQL")
    void testUserPersistenceAndLookup() {
        checkDatabase();

        String rawPassword = "PostgresPassword2026!";
        String hash = PasswordHasher.hashPassword(rawPassword);
        User user = new User(
                "IT-USER-PERSIST", "Postgres User", "pg.user@example.com",
                "+1-555-0909", hash, UserRole.CUSTOMER, "DL-PG-999", true, LocalDateTime.now()
        );

        userRepository.save(user);

        // Lookup by ID
        Optional<User> byId = userRepository.findById("IT-USER-PERSIST");
        assertTrue(byId.isPresent());
        assertEquals("Postgres User", byId.get().getFullName());
        assertEquals(UserRole.CUSTOMER, byId.get().getRole());

        // Lookup by uppercase normalized email
        Optional<User> byEmail = userRepository.findByEmail("PG.USER@EXAMPLE.COM");
        assertTrue(byEmail.isPresent());
        assertEquals("IT-USER-PERSIST", byEmail.get().getId());

        // Password hash verification against stored DB hash
        assertTrue(PasswordHasher.checkPassword(rawPassword, byEmail.get().getPasswordHash()));
        assertFalse(PasswordHasher.checkPassword("WrongPass!", byEmail.get().getPasswordHash()));
    }

    @Test
    @Order(15)
    @DisplayName("15. Enforcing duplicate email rejection in PostgreSQL users table")
    void testDuplicateEmailRejection() {
        checkDatabase();

        User duplicateEmailUser = new User(
                "IT-USER-DUP", "Another User", "pg.user@example.com",
                "+1-555-0910", "$hash", UserRole.CUSTOMER, null, true, LocalDateTime.now()
        );

        assertThrows(DataAccessException.class, () -> userRepository.save(duplicateEmailUser));
    }

    @Test
    @Order(16)
    @DisplayName("16. Enforcing role validation for CUSTOMER and ADMIN")
    void testRoleValidation() {
        checkDatabase();

        User admin = new User(
                "IT-ADMIN-1", "Database Admin", "it.admin@ready2ride.internal",
                "+1-555-0911", PasswordHasher.hashPassword("AdminPass2026!"),
                UserRole.ADMIN, null, true, LocalDateTime.now()
        );

        userRepository.save(admin);

        Optional<User> retrievedAdmin = userRepository.findById("IT-ADMIN-1");
        assertTrue(retrievedAdmin.isPresent());
        assertEquals(UserRole.ADMIN, retrievedAdmin.get().getRole());
        assertTrue(retrievedAdmin.get().isAdmin());

        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        assertTrue(admins.stream().anyMatch(u -> "IT-ADMIN-1".equals(u.getId())));
    }

    @Test
    @Order(17)
    @DisplayName("17. Rejecting bookings for inactive (deactivated) vehicles")
    void testInactiveVehicleBookingRejection() {
        checkDatabase();

        Vehicle inactiveVehicle = new Car(
                "IT-CAR-INACTIVE", "Ford", "Focus", "IT-REG-INACT-1",
                new BigDecimal("40.00"), 5, "Petrol"
        );
        inactiveVehicle.setActive(false);
        vehicleRepository.save(inactiveVehicle);

        Booking booking = new Booking(
                "IT-BKG-INACT-V", TEST_CUSTOMER_ID, "IT-CAR-INACTIVE",
                BASE_TIME.plusDays(30), BASE_TIME.plusDays(32), new BigDecimal("80.00"), BookingStatus.CONFIRMED
        );

        assertThrows(VehicleNotAvailableException.class, () -> bookingRepository.save(booking));
    }

    @Test
    @Order(18)
    @DisplayName("18. Payment persistence and lookup in PostgreSQL")
    void testPaymentPersistence() {
        checkDatabase();

        Payment payment = new Payment(
                "IT-PAY-1", "IT-BKG-1", new BigDecimal("99.00"),
                LocalDateTime.now(), PaymentMethod.UPI, PaymentStatus.COMPLETED, "UPI-IT-TXN-12345"
        );

        paymentRepository.save(payment);

        Optional<Payment> retrieved = paymentRepository.findById("IT-PAY-1");
        assertTrue(retrieved.isPresent());
        assertEquals("IT-BKG-1", retrieved.get().getBookingId());
        assertEquals(new BigDecimal("99.00"), retrieved.get().getAmount());
        assertEquals("UPI-IT-TXN-12345", retrieved.get().getTransactionReference());
        assertEquals(PaymentMethod.UPI, retrieved.get().getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, retrieved.get().getStatus());

        List<Payment> byBooking = paymentRepository.findByBookingId("IT-BKG-1");
        assertFalse(byBooking.isEmpty());
        assertTrue(byBooking.stream().anyMatch(p -> "IT-PAY-1".equals(p.getPaymentId())));
    }

    @Test
    @Order(19)
    @DisplayName("19. Maintenance persistence and vehicle schedule blocking in PostgreSQL")
    void testMaintenancePersistenceAndAvailabilityConflict() {
        checkDatabase();

        LocalDate mntDate = BASE_TIME.plusDays(40).toLocalDate();
        MaintenanceRecord record = new MaintenanceRecord(
                "IT-MNT-1", "IT-CAR-1", mntDate,
                "Brake overhaul and full inspection", new BigDecimal("180.00"), false
        );

        maintenanceRepository.save(record);

        Optional<MaintenanceRecord> retrieved = maintenanceRepository.findById("IT-MNT-1");
        assertTrue(retrieved.isPresent());
        assertEquals("IT-CAR-1", retrieved.get().getVehicleId());
        assertFalse(retrieved.get().isCompleted());

        // Booking overlapping with the scheduled maintenance date must be rejected
        LocalDateTime mntBookingStart = BASE_TIME.plusDays(39);
        LocalDateTime mntBookingEnd = BASE_TIME.plusDays(42);

        Booking conflictBooking = new Booking(
                "IT-BKG-MNT-CONFLICT", TEST_CUSTOMER_ID, "IT-CAR-1",
                mntBookingStart, mntBookingEnd, new BigDecimal("120.00"), BookingStatus.CONFIRMED
        );

        assertThrows(VehicleNotAvailableException.class, () -> bookingRepository.save(conflictBooking));

        // Mark maintenance as completed - should no longer block booking
        record.setCompleted(true);
        maintenanceRepository.save(record);

        Booking allowedAfterMnt = new Booking(
                "IT-BKG-MNT-RESOLVED", TEST_CUSTOMER_ID, "IT-CAR-1",
                mntBookingStart, mntBookingEnd, new BigDecimal("120.00"), BookingStatus.CONFIRMED
        );
        Booking saved = bookingRepository.save(allowedAfterMnt);
        assertNotNull(saved);
        assertEquals("IT-BKG-MNT-RESOLVED", saved.getBookingId());
    }
}

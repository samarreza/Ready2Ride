package com.ready2ride.core.database;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.PaymentMethod;
import com.ready2ride.core.model.PaymentStatus;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.repository.BookingRepository;
import com.ready2ride.core.repository.PaymentRepository;
import com.ready2ride.core.repository.UserRepository;
import com.ready2ride.core.repository.VehicleImageRepository;
import com.ready2ride.core.repository.VehicleRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Safe, idempotent data initializer that seeds realistic demonstration fleet vehicles
 * and their actual raw binary images into PostgreSQL.
 * <p>
 * Ensures:
 * <ul>
 *     <li>Idempotent execution: does not produce duplicate records or overwrite administrator-managed fleet.</li>
 *     <li>Loads bundled image bytes from resources or filesystem and writes them directly into PostgreSQL BYTEA columns.</li>
 *     <li>Never drops tables or removes unrelated records.</li>
 * </ul>
 * </p>
 */
public class DemoDataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DemoDataInitializer.class.getName());

    public static final String DEMO_CAR_ID = "CAR-DEMO-101";
    public static final String DEMO_BIKE_ID = "BIKE-DEMO-201";
    public static final String DEMO_SUV_ID = "SUV-DEMO-301";
    public static final String DEMO_VAN_ID = "VAN-DEMO-401";

    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository imageRepository;

    public DemoDataInitializer(VehicleRepository vehicleRepository, VehicleImageRepository imageRepository) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository must not be null");
        this.imageRepository = Objects.requireNonNull(imageRepository, "VehicleImageRepository must not be null");
    }

    /**
     * Seeds demo vehicles and image bytes if not already present.
     */
    public synchronized void seedDemoFleetIfAbsent() {
        try {
            seedCar();
            seedBike();
            seedSUV();
            seedVan();
            LOGGER.info("Demo fleet and image byte initialization check completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to complete demo fleet initialization: " + e.getMessage(), e);
        }
    }

    /**
     * Seeds additional realistic fleet items matching the reference screenshot catalog.
     */
    public synchronized void seedExtendedFleetIfAbsent() {
        try {
            seedExtendedVehicles();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to complete extended fleet initialization: " + e.getMessage(), e);
        }
    }

    /**
     * Seeds initial demonstration users (admin and customer) with secure password hashes.
     */
    public synchronized void seedDemoUsersIfAbsent(com.ready2ride.core.repository.UserRepository userRepo) {
        if (userRepo == null) return;
        try {
            if (!userRepo.existsByEmail("admin@ready2ride.com")) {
                com.ready2ride.core.model.User admin = new com.ready2ride.core.model.User(
                        "USR-ADMIN-001",
                        "Fleet Administrator",
                        "admin@ready2ride.com",
                        "+91 98765 12345",
                        com.ready2ride.core.util.PasswordHasher.hashPassword("Admin@123"),
                        com.ready2ride.core.model.UserRole.ADMIN,
                        null
                );
                userRepo.save(admin);
                LOGGER.info("Seeded demo admin account: admin@ready2ride.com");
            }

            if (!userRepo.existsByEmail("provider@ready2ride.com")) {
                com.ready2ride.core.model.User provider = new com.ready2ride.core.model.User(
                        "PROV-DEV-001",
                        "Rajesh Sharma (Apex Fleet Rentals)",
                        "provider@ready2ride.com",
                        "+91 98111 22334",
                        com.ready2ride.core.util.PasswordHasher.hashPassword("Provider@123"),
                        com.ready2ride.core.model.UserRole.RENTAL_PROVIDER,
                        "DL-PROV-55443"
                );
                userRepo.save(provider);
                LOGGER.info("Seeded demo rental provider account: provider@ready2ride.com");
            }

            if (!userRepo.existsByEmail("john.doe@ready2ride.com")) {
                com.ready2ride.core.model.User customer = new com.ready2ride.core.model.User(
                        "CUST-DEV-001",
                        "John Doe",
                        "john.doe@ready2ride.com",
                        "+91 98765 43210",
                        com.ready2ride.core.util.PasswordHasher.hashPassword("Customer@123"),
                        com.ready2ride.core.model.UserRole.CUSTOMER,
                        "DL-2024-98765"
                );
                userRepo.save(customer);
                LOGGER.info("Seeded demo customer account: john.doe@ready2ride.com");
            }

            if (!userRepo.existsByEmail("dhruv@example.com")) {
                com.ready2ride.core.model.User dhruv = new com.ready2ride.core.model.User(
                        "CUST-DHRUV-001",
                        "Dhruv Taneja",
                        "dhruv@example.com",
                        "+91 98765 43210",
                        com.ready2ride.core.util.PasswordHasher.hashPassword("Customer@123"),
                        com.ready2ride.core.model.UserRole.CUSTOMER,
                        "DL-2026-DHRUV"
                );
                userRepo.save(dhruv);
                LOGGER.info("Seeded demo customer account: dhruv@example.com");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Notice during demo users seeding: " + e.getMessage(), e);
        }
    }

    /**
     * Preserved for backward compatibility. Real customer reservations are strictly created
     * through user actions in the application; demo/fake booking generation is disabled.
     */
    public synchronized void seedDemoBookingsIfAbsent(BookingRepository bookingRepo, UserRepository userRepo, PaymentRepository paymentRepo) {
        // Disabled per Task 4: Do not fabricate fake customer reservations or payment transactions.
    }

    private void seedExtendedVehicles() {
        if (!vehicleRepository.existsById("CAR-EXT-102")) {
            Car car2 = new Car("CAR-EXT-102", "Hyundai", "i20 Magna", "MH-12-RR-1002", BigDecimal.valueOf(1999.00), 5, "Petrol", 2);
            vehicleRepository.save(car2);
            seedImageIfAbsent("IMG-CAR-EXT-102", "CAR-EXT-102", "car_sedan.jpg", "image/jpeg");
        }
        if (!vehicleRepository.existsById("BIKE-EXT-202")) {
            Bike bike2 = new Bike("BIKE-EXT-202", "Royal Enfield", "Classic 350", "MH-12-RR-2003", BigDecimal.valueOf(999.00), 349, 40);
            vehicleRepository.save(bike2);
            seedImageIfAbsent("IMG-BIKE-EXT-202", "BIKE-EXT-202", "bike_motorcycle.jpg", "image/jpeg");
        }
        if (!vehicleRepository.existsById("SUV-EXT-302")) {
            SUV suv2 = new SUV("SUV-EXT-302", "Hyundai", "Creta SX", "MH-12-RR-3004", BigDecimal.valueOf(3499.00), false, 5, 3, "Petrol");
            vehicleRepository.save(suv2);
            seedImageIfAbsent("IMG-SUV-EXT-302", "SUV-EXT-302", "suv_dark.jpg", "image/jpeg");
        }
        if (!vehicleRepository.existsById("CAR-EXT-103")) {
            Car car3 = new Car("CAR-EXT-103", "Maruti Suzuki", "Swift ZXi", "MH-12-RR-1003", BigDecimal.valueOf(1799.00), 5, "Petrol", 2);
            vehicleRepository.save(car3);
            seedImageIfAbsent("IMG-CAR-EXT-103", "CAR-EXT-103", "car_sedan.jpg", "image/jpeg");
        }

        if (vehicleRepository.existsById("CAR-DEV-101")) {
            seedImageIfAbsent("IMG-CAR-DEV-101", "CAR-DEV-101", "car_sedan.jpg", "image/jpeg");
        }
        if (vehicleRepository.existsById("BIKE-DEV-201")) {
            seedImageIfAbsent("IMG-BIKE-DEV-201", "BIKE-DEV-201", "bike_motorcycle.jpg", "image/jpeg");
        }
        if (vehicleRepository.existsById("SUV-DEV-301")) {
            seedImageIfAbsent("IMG-SUV-DEV-301", "SUV-DEV-301", "suv_dark.jpg", "image/jpeg");
        }
        if (vehicleRepository.existsById("VAN-DEV-401")) {
            seedImageIfAbsent("IMG-VAN-DEV-401", "VAN-DEV-401", "van_white.jpg", "image/jpeg");
        }
    }

    private void seedCar() {
        if (!vehicleRepository.existsById(DEMO_CAR_ID)) {
            Car car = new Car(
                    DEMO_CAR_ID,
                    "Toyota",
                    "Camry Hybrid",
                    "MH-12-RR-1001",
                    BigDecimal.valueOf(2499.00),
                    5,
                    "Petrol",
                    2
            );
            vehicleRepository.save(car);
            LOGGER.info("Persisted demo car: " + DEMO_CAR_ID);
        }

        seedImageIfAbsent("IMG-CAR-DEMO-101", DEMO_CAR_ID, "car_sedan.jpg", "image/jpeg");
    }

    private void seedBike() {
        if (!vehicleRepository.existsById(DEMO_BIKE_ID)) {
            Bike bike = new Bike(
                    DEMO_BIKE_ID,
                    "Yamaha",
                    "MT-09 Street",
                    "MH-12-RR-2002",
                    BigDecimal.valueOf(799.00),
                    890,
                    45
            );
            vehicleRepository.save(bike);
            LOGGER.info("Persisted demo bike: " + DEMO_BIKE_ID);
        }

        seedImageIfAbsent("IMG-BIKE-DEMO-201", DEMO_BIKE_ID, "bike_motorcycle.jpg", "image/jpeg");
    }

    private void seedSUV() {
        if (!vehicleRepository.existsById(DEMO_SUV_ID)) {
            SUV suv = new SUV(
                    DEMO_SUV_ID,
                    "Toyota",
                    "Fortuner 4x4",
                    "MH-12-RR-3003",
                    BigDecimal.valueOf(3999.00),
                    true,
                    7,
                    3,
                    "Diesel"
            );
            vehicleRepository.save(suv);
            LOGGER.info("Persisted demo SUV: " + DEMO_SUV_ID);
        }

        seedImageIfAbsent("IMG-SUV-DEMO-301", DEMO_SUV_ID, "suv_dark.jpg", "image/jpeg");
    }

    private void seedVan() {
        if (!vehicleRepository.existsById(DEMO_VAN_ID)) {
            Van van = new Van(
                    DEMO_VAN_ID,
                    "Mercedes-Benz",
                    "Sprinter Passenger",
                    "MH-12-RR-4004",
                    BigDecimal.valueOf(4999.00),
                    1500.0,
                    12,
                    5,
                    "Diesel"
            );
            vehicleRepository.save(van);
            LOGGER.info("Persisted demo van: " + DEMO_VAN_ID);
        }

        seedImageIfAbsent("IMG-VAN-DEMO-401", DEMO_VAN_ID, "van_white.jpg", "image/jpeg");
    }

    private void seedImageIfAbsent(String imageId, String vehicleId, String resourceFileName, String contentType) {
        if (imageRepository.findById(imageId).isPresent() || imageRepository.findPrimaryByVehicleId(vehicleId).isPresent()) {
            return; // Already seeded, avoid redundant writes
        }

        byte[] imageBytes = loadResourceImageBytes(resourceFileName);
        if (imageBytes != null && imageBytes.length > 0) {
            VehicleImage image = new VehicleImage(imageId, vehicleId, resourceFileName, contentType, imageBytes, true);
            imageRepository.save(image);
            LOGGER.info(String.format("Persisted image bytes for %s (%d bytes) in PostgreSQL.", vehicleId, imageBytes.length));
        } else {
            LOGGER.warning("Could not locate resource image bytes for: " + resourceFileName);
        }
    }

    /**
     * Attempts to load image bytes first from ClassLoader resource, then from local workspace paths.
     */
    public static byte[] loadResourceImageBytes(String fileName) {
        // 1. Try classloader resource
        try (InputStream in = DemoDataInitializer.class.getResourceAsStream("/images/" + fileName)) {
            if (in != null) {
                return in.readAllBytes();
            }
        } catch (Exception ignored) {
        }

        // 2. Try file system paths in workspace
        String[] potentialPaths = {
                "ready2ride-core/src/main/resources/images/" + fileName,
                "ready2ride-desktop/src/main/resources/images/" + fileName,
                "src/main/resources/images/" + fileName,
                "../ready2ride-desktop/src/main/resources/images/" + fileName,
                "../ready2ride-core/src/main/resources/images/" + fileName
        };

        for (String p : potentialPaths) {
            Path path = Paths.get(p);
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }
}

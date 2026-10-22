package com.ready2ride.core.repository;

import com.ready2ride.core.database.DatabaseConfig;
import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.repository.jdbc.JdbcVehicleImageRepository;
import com.ready2ride.core.repository.jdbc.JdbcVehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleImageRepositoryTest {

    @Test
    @DisplayName("Should save, retrieve, and delete vehicle image in InMemoryVehicleImageRepository")
    void testInMemoryVehicleImageRepository() {
        VehicleImageRepository repo = new InMemoryVehicleImageRepository();

        byte[] fakeData = "FakeImageDataBytes123456789".getBytes(StandardCharsets.UTF_8);
        VehicleImage img = new VehicleImage("IMG-TEST-1", "CAR-101", "car.png", "image/png", fakeData, true);

        repo.save(img);

        Optional<VehicleImage> retrieved = repo.findById("IMG-TEST-1");
        assertTrue(retrieved.isPresent());
        assertEquals("CAR-101", retrieved.get().getVehicleId());
        assertEquals("car.png", retrieved.get().getFileName());
        assertEquals("image/png", retrieved.get().getContentType());
        assertTrue(retrieved.get().isPrimary());
        assertArrayEquals(fakeData, retrieved.get().getImageData());

        Optional<VehicleImage> primary = repo.findPrimaryByVehicleId("CAR-101");
        assertTrue(primary.isPresent());
        assertEquals("IMG-TEST-1", primary.get().getId());

        List<VehicleImage> list = repo.findByVehicleId("CAR-101");
        assertEquals(1, list.size());

        assertTrue(repo.deleteById("IMG-TEST-1"));
        assertFalse(repo.findById("IMG-TEST-1").isPresent());
    }

    @Test
    @DisplayName("Should persist and retrieve actual binary bytes in PostgreSQL JdbcVehicleImageRepository if database is available")
    void testJdbcVehicleImageRepository() {
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(DatabaseConfig.fromEnvironment());
        if (!connectionManager.isDatabaseAvailable(2)) {
            return; // Cleanly skip if live db unavailable
        }

        JdbcVehicleRepository vehicleRepo = new JdbcVehicleRepository(connectionManager);
        JdbcVehicleImageRepository imageRepo = new JdbcVehicleImageRepository(connectionManager);

        String testVehicleId = "VEH-IMG-TEST-" + System.currentTimeMillis();
        Car testCar = new Car(testVehicleId, "TestBrand", "TestModel", "REG-IMG-" + System.currentTimeMillis(),
                BigDecimal.valueOf(2500.00), 5, "Petrol", 2);
        vehicleRepo.save(testCar);

        try {
            byte[] binaryPayload = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46 };
            String imageId = "IMG-" + testVehicleId;
            VehicleImage image = new VehicleImage(imageId, testVehicleId, "test_camry.jpg", "image/jpeg", binaryPayload, true);

            imageRepo.save(image);

            Optional<VehicleImage> retrieved = imageRepo.findById(imageId);
            assertTrue(retrieved.isPresent(), "Image should be retrieved from PostgreSQL");
            assertEquals(testVehicleId, retrieved.get().getVehicleId());
            assertEquals("test_camry.jpg", retrieved.get().getFileName());
            assertEquals("image/jpeg", retrieved.get().getContentType());
            assertTrue(retrieved.get().isPrimary());
            assertArrayEquals(binaryPayload, retrieved.get().getImageData(), "Binary image payload bytes must match exactly");

            Optional<VehicleImage> primary = imageRepo.findPrimaryByVehicleId(testVehicleId);
            assertTrue(primary.isPresent());
            assertEquals(imageId, primary.get().getId());

            // Clean up
            imageRepo.deleteById(imageId);
            assertFalse(imageRepo.findById(imageId).isPresent());
        } finally {
            vehicleRepo.deleteById(testVehicleId);
        }
    }
}

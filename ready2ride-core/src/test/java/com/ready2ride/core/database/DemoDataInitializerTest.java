package com.ready2ride.core.database;

import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.repository.InMemoryVehicleImageRepository;
import com.ready2ride.core.repository.InMemoryVehicleRepository;
import com.ready2ride.core.repository.VehicleImageRepository;
import com.ready2ride.core.repository.VehicleRepository;
import com.ready2ride.core.repository.jdbc.JdbcVehicleImageRepository;
import com.ready2ride.core.repository.jdbc.JdbcVehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataInitializerTest {

    @Test
    @DisplayName("Should seed demo vehicles into in-memory repositories safely")
    void testInMemorySeeding() {
        VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
        VehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

        DemoDataInitializer initializer = new DemoDataInitializer(vehicleRepo, imageRepo);
        initializer.seedDemoFleetIfAbsent();

        assertTrue(vehicleRepo.existsById(DemoDataInitializer.DEMO_CAR_ID));
        assertTrue(vehicleRepo.existsById(DemoDataInitializer.DEMO_BIKE_ID));
        assertTrue(vehicleRepo.existsById(DemoDataInitializer.DEMO_SUV_ID));
        assertTrue(vehicleRepo.existsById(DemoDataInitializer.DEMO_VAN_ID));

        // Repeat to ensure idempotent
        initializer.seedDemoFleetIfAbsent();
        assertEquals(4, vehicleRepo.findAll().size());
    }

    @Test
    @DisplayName("Should seed demo vehicles and image bytes into live PostgreSQL database")
    void testLivePostgresSeeding() {
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(DatabaseConfig.fromEnvironment());
        if (!connectionManager.isDatabaseAvailable(2)) {
            return;
        }

        JdbcVehicleRepository vehicleRepo = new JdbcVehicleRepository(connectionManager);
        JdbcVehicleImageRepository imageRepo = new JdbcVehicleImageRepository(connectionManager);

        DemoDataInitializer initializer = new DemoDataInitializer(vehicleRepo, imageRepo);
        initializer.seedDemoFleetIfAbsent();

        // Verify Car
        Optional<Vehicle> car = vehicleRepo.findById(DemoDataInitializer.DEMO_CAR_ID);
        assertTrue(car.isPresent(), "Demo Car must exist in PostgreSQL");
        assertEquals("Toyota", car.get().getBrand());
        assertEquals("Camry Hybrid", car.get().getModel());
        assertEquals(2499.00, car.get().getRentalRate().doubleValue());

        Optional<VehicleImage> carImage = imageRepo.findPrimaryByVehicleId(DemoDataInitializer.DEMO_CAR_ID);
        assertTrue(carImage.isPresent(), "Demo Car image bytes must exist in PostgreSQL BYTEA table");
        assertNotNull(carImage.get().getImageData());
        assertTrue(carImage.get().getImageData().length > 1000, "Image bytes should contain full image data");

        // Verify Bike
        Optional<Vehicle> bike = vehicleRepo.findById(DemoDataInitializer.DEMO_BIKE_ID);
        assertTrue(bike.isPresent(), "Demo Bike must exist in PostgreSQL");
        Optional<VehicleImage> bikeImage = imageRepo.findPrimaryByVehicleId(DemoDataInitializer.DEMO_BIKE_ID);
        assertTrue(bikeImage.isPresent());

        // Verify SUV
        Optional<Vehicle> suv = vehicleRepo.findById(DemoDataInitializer.DEMO_SUV_ID);
        assertTrue(suv.isPresent(), "Demo SUV must exist in PostgreSQL");
        Optional<VehicleImage> suvImage = imageRepo.findPrimaryByVehicleId(DemoDataInitializer.DEMO_SUV_ID);
        assertTrue(suvImage.isPresent());

        // Verify Van
        Optional<Vehicle> van = vehicleRepo.findById(DemoDataInitializer.DEMO_VAN_ID);
        assertTrue(van.isPresent(), "Demo Van must exist in PostgreSQL");
        Optional<VehicleImage> vanImage = imageRepo.findPrimaryByVehicleId(DemoDataInitializer.DEMO_VAN_ID);
        assertTrue(vanImage.isPresent());

        // Run second time to ensure idempotency and no errors or duplicate keys
        initializer.seedDemoFleetIfAbsent();
    }
}

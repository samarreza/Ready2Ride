package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleType;
import com.ready2ride.core.repository.VehicleRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL JDBC-backed implementation of {@link VehicleRepository}.
 * Manages persistent storage and polymorphic reconstruction of fleet vehicle entities.
 */
public class JdbcVehicleRepository implements VehicleRepository {

    private final DatabaseConnectionManager connectionManager;

    public JdbcVehicleRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");

        String checkSql = "SELECT 1 FROM vehicles WHERE id = ?";
        String updateSql = """
                UPDATE vehicles
                SET vehicle_type = ?, brand = ?, model = ?, registration_number = ?,
                    rental_rate = ?, is_active = ?, seating_capacity = ?, fuel_type = ?,
                    engine_capacity_cc = ?, four_wheel_drive = ?, cargo_capacity_kg = ?,
                    luggage_capacity_bags = ?, mileage_kmpl = ?, provider_id = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        String insertSql = """
                INSERT INTO vehicles (
                    id, vehicle_type, brand, model, registration_number, rental_rate,
                    is_active, seating_capacity, fuel_type, engine_capacity_cc,
                    four_wheel_drive, cargo_capacity_kg, luggage_capacity_bags, mileage_kmpl, provider_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionManager.getConnection()) {
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, vehicle.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    bindVehicleParameters(updateStmt, vehicle, 1);
                    updateStmt.setString(15, vehicle.getId());
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, vehicle.getId());
                    bindVehicleParameters(insertStmt, vehicle, 2);
                    insertStmt.executeUpdate();
                }
            }

            return vehicle;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save vehicle with ID '" + vehicle.getId() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM vehicles WHERE id = ? AND is_active = TRUE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVehicle(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query vehicle by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Vehicle> findAll() {
        String sql = "SELECT * FROM vehicles WHERE is_active = TRUE ORDER BY brand, model";
        List<Vehicle> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToVehicle(rs));
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve all vehicles: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }

        String sql = "SELECT 1 FROM vehicles WHERE id = ? AND is_active = TRUE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check vehicle existence for ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }

        // Soft-delete to prevent foreign key constraint violations against historical bookings
        String sql = "UPDATE vehicles SET is_active = FALSE WHERE id = ? AND is_active = TRUE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete vehicle with ID '" + id + "': " + e.getMessage(), e);
        }
    }

    private void bindVehicleParameters(PreparedStatement stmt, Vehicle vehicle, int startIndex) throws SQLException {
        stmt.setString(startIndex, vehicle.getVehicleType().name());
        stmt.setString(startIndex + 1, vehicle.getBrand());
        stmt.setString(startIndex + 2, vehicle.getModel());
        stmt.setString(startIndex + 3, vehicle.getRegistrationNumber());
        stmt.setBigDecimal(startIndex + 4, vehicle.getRentalRate());
        stmt.setBoolean(startIndex + 5, vehicle.isActive());

        // Subclass-specific parameter bindings
        if (vehicle instanceof Car car) {
            stmt.setInt(startIndex + 6, car.getSeatingCapacity());
            stmt.setString(startIndex + 7, car.getFuelType());
            stmt.setNull(startIndex + 8, Types.INTEGER);
            stmt.setNull(startIndex + 9, Types.BOOLEAN);
            stmt.setNull(startIndex + 10, Types.NUMERIC);
            stmt.setInt(startIndex + 11, car.getLuggageCapacityBags());
            stmt.setNull(startIndex + 12, Types.INTEGER);
        } else if (vehicle instanceof Bike bike) {
            stmt.setInt(startIndex + 6, bike.getSeatingCapacity());
            stmt.setString(startIndex + 7, bike.getFuelType());
            stmt.setInt(startIndex + 8, bike.getEngineCapacityCc());
            stmt.setNull(startIndex + 9, Types.BOOLEAN);
            stmt.setNull(startIndex + 10, Types.NUMERIC);
            stmt.setNull(startIndex + 11, Types.INTEGER);
            stmt.setInt(startIndex + 12, bike.getMileageKmpl());
        } else if (vehicle instanceof SUV suv) {
            stmt.setInt(startIndex + 6, suv.getSeatingCapacity());
            stmt.setString(startIndex + 7, suv.getFuelType());
            stmt.setNull(startIndex + 8, Types.INTEGER);
            stmt.setBoolean(startIndex + 9, suv.isFourWheelDrive());
            stmt.setNull(startIndex + 10, Types.NUMERIC);
            stmt.setInt(startIndex + 11, suv.getLuggageCapacityBags());
            stmt.setNull(startIndex + 12, Types.INTEGER);
        } else if (vehicle instanceof Van van) {
            stmt.setInt(startIndex + 6, van.getSeatingCapacity());
            stmt.setString(startIndex + 7, van.getFuelType());
            stmt.setNull(startIndex + 8, Types.INTEGER);
            stmt.setNull(startIndex + 9, Types.BOOLEAN);
            stmt.setBigDecimal(startIndex + 10, BigDecimal.valueOf(van.getCargoCapacityKg()));
            stmt.setInt(startIndex + 11, van.getLuggageCapacityBags());
            stmt.setNull(startIndex + 12, Types.INTEGER);
        } else {
            stmt.setNull(startIndex + 6, Types.INTEGER);
            stmt.setNull(startIndex + 7, Types.VARCHAR);
            stmt.setNull(startIndex + 8, Types.INTEGER);
            stmt.setNull(startIndex + 9, Types.BOOLEAN);
            stmt.setNull(startIndex + 10, Types.NUMERIC);
            stmt.setNull(startIndex + 11, Types.INTEGER);
            stmt.setNull(startIndex + 12, Types.INTEGER);
        }

        // Parameter 14: provider_id (foreign key to users table)
        if (vehicle.getProviderId() != null && !vehicle.getProviderId().isBlank()) {
            stmt.setString(startIndex + 13, vehicle.getProviderId().trim());
        } else {
            stmt.setNull(startIndex + 13, Types.VARCHAR);
        }
    }

    @Override
    public List<Vehicle> findByProviderId(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM vehicles WHERE provider_id = ? ORDER BY created_at DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, providerId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                List<Vehicle> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRowToVehicle(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query vehicles by provider ID '" + providerId + "': " + e.getMessage(), e);
        }
    }

    private Vehicle mapRowToVehicle(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String typeStr = rs.getString("vehicle_type");
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        String regNumber = rs.getString("registration_number");
        BigDecimal rate = rs.getBigDecimal("rental_rate");
        boolean active = rs.getBoolean("is_active");
        int luggageBags = rs.getInt("luggage_capacity_bags");
        int mileage = rs.getInt("mileage_kmpl");

        VehicleType type = VehicleType.valueOf(typeStr);

        Vehicle vehicle = switch (type) {
            case CAR -> {
                int seats = rs.getInt("seating_capacity");
                String fuel = rs.getString("fuel_type");
                int bags = luggageBags > 0 ? luggageBags : 2;
                yield new Car(id, brand, model, regNumber, rate, (seats > 0 ? seats : 5), (fuel != null ? fuel : "Petrol"), bags);
            }
            case BIKE -> {
                int cc = rs.getInt("engine_capacity_cc");
                int kmpl = mileage > 0 ? mileage : 45;
                yield new Bike(id, brand, model, regNumber, rate, (cc > 0 ? cc : 150), kmpl);
            }
            case SUV -> {
                boolean fwd = rs.getBoolean("four_wheel_drive");
                int seats = rs.getInt("seating_capacity");
                int bags = luggageBags > 0 ? luggageBags : 3;
                yield new SUV(id, brand, model, regNumber, rate, fwd, (seats > 0 ? seats : 7), bags);
            }
            case VAN -> {
                BigDecimal cargo = rs.getBigDecimal("cargo_capacity_kg");
                double cargoVal = (cargo != null ? cargo.doubleValue() : 1000.0);
                int seats = rs.getInt("seating_capacity");
                int bags = luggageBags > 0 ? luggageBags : 5;
                yield new Van(id, brand, model, regNumber, rate, cargoVal, (seats > 0 ? seats : 12), bags);
            }
        };

        vehicle.setActive(active);
        try {
            String provId = rs.getString("provider_id");
            vehicle.setProviderId(provId);
        } catch (SQLException ignored) {
            // column may not exist yet in legacy or mock contexts
        }
        return vehicle;
    }
}

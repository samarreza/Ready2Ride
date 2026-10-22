package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.repository.VehicleImageRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL JDBC-backed implementation of {@link VehicleImageRepository}.
 * Stores and retrieves actual binary image bytes using PostgreSQL BYTEA data type.
 */
public class JdbcVehicleImageRepository implements VehicleImageRepository {

    private final DatabaseConnectionManager connectionManager;

    public JdbcVehicleImageRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    @Override
    public VehicleImage save(VehicleImage image) {
        Objects.requireNonNull(image, "VehicleImage must not be null");

        String checkSql = "SELECT 1 FROM vehicle_images WHERE id = ?";
        String updateSql = """
                UPDATE vehicle_images
                SET file_name = ?, content_type = ?, image_data = ?, is_primary = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        String insertSql = """
                INSERT INTO vehicle_images (id, vehicle_id, file_name, content_type, image_data, is_primary, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        try (Connection conn = connectionManager.getConnection()) {
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, image.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, image.getFileName());
                    updateStmt.setString(2, image.getContentType());
                    updateStmt.setBytes(3, image.getImageData());
                    updateStmt.setBoolean(4, image.isPrimary());
                    updateStmt.setString(5, image.getId());
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, image.getId());
                    insertStmt.setString(2, image.getVehicleId());
                    insertStmt.setString(3, image.getFileName());
                    insertStmt.setString(4, image.getContentType());
                    insertStmt.setBytes(5, image.getImageData());
                    insertStmt.setBoolean(6, image.isPrimary());
                    insertStmt.executeUpdate();
                }
            }

            return image;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to persist vehicle image '" + image.getId() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<VehicleImage> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM vehicle_images WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVehicleImage(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query vehicle image by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<VehicleImage> findByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM vehicle_images WHERE vehicle_id = ? ORDER BY is_primary DESC, created_at ASC";
        List<VehicleImage> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToVehicleImage(rs));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query vehicle images for vehicle '" + vehicleId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<VehicleImage> findPrimaryByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
        }

        // Search for explicitly flagged primary first, or fallback to first image for vehicle
        String sql = "SELECT * FROM vehicle_images WHERE vehicle_id = ? ORDER BY is_primary DESC, created_at ASC LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVehicleImage(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query primary image for vehicle '" + vehicleId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM vehicle_images WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete vehicle image by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return false;
        }

        String sql = "DELETE FROM vehicle_images WHERE vehicle_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete vehicle images for vehicle '" + vehicleId + "': " + e.getMessage(), e);
        }
    }

    private VehicleImage mapRowToVehicleImage(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String vehicleId = rs.getString("vehicle_id");
        String fileName = rs.getString("file_name");
        String contentType = rs.getString("content_type");
        byte[] imageData = rs.getBytes("image_data");
        boolean isPrimary = rs.getBoolean("is_primary");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        LocalDateTime createdLdt = createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now();
        LocalDateTime updatedLdt = updatedAt != null ? updatedAt.toLocalDateTime() : LocalDateTime.now();

        return new VehicleImage(id, vehicleId, fileName, contentType, imageData, isPrimary, createdLdt, updatedLdt);
    }
}

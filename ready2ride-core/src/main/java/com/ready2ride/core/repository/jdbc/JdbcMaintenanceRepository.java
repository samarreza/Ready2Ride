package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.model.MaintenanceRecord;
import com.ready2ride.core.repository.MaintenanceRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL JDBC-backed implementation of {@link MaintenanceRepository}.
 */
public class JdbcMaintenanceRepository implements MaintenanceRepository {

    private final DatabaseConnectionManager connectionManager;

    public JdbcMaintenanceRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    @Override
    public MaintenanceRecord save(MaintenanceRecord record) {
        Objects.requireNonNull(record, "MaintenanceRecord must not be null");

        String checkSql = "SELECT 1 FROM maintenance WHERE id = ?";
        String updateSql = """
                UPDATE maintenance
                SET vehicle_id = ?, service_date = ?, description = ?,
                    cost = ?, is_completed = ?
                WHERE id = ?
                """;
        String insertSql = """
                INSERT INTO maintenance (
                    id, vehicle_id, service_date, description, cost, is_completed
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionManager.getConnection()) {
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, record.getRecordId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, record.getVehicleId());
                    updateStmt.setDate(2, Date.valueOf(record.getServiceDate()));
                    updateStmt.setString(3, record.getDescription());
                    updateStmt.setBigDecimal(4, record.getCost());
                    updateStmt.setBoolean(5, record.isCompleted());
                    updateStmt.setString(6, record.getRecordId());
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, record.getRecordId());
                    insertStmt.setString(2, record.getVehicleId());
                    insertStmt.setDate(3, Date.valueOf(record.getServiceDate()));
                    insertStmt.setString(4, record.getDescription());
                    insertStmt.setBigDecimal(5, record.getCost());
                    insertStmt.setBoolean(6, record.isCompleted());
                    insertStmt.executeUpdate();
                }
            }

            return record;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to persist maintenance record with ID '" + record.getRecordId() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MaintenanceRecord> findById(String recordId) {
        if (recordId == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM maintenance WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRecord(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query maintenance record by ID '" + recordId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaintenanceRecord> findByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM maintenance WHERE vehicle_id = ? ORDER BY service_date DESC";
        List<MaintenanceRecord> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query maintenance records for vehicle '" + vehicleId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        String sql = "SELECT * FROM maintenance ORDER BY service_date DESC";
        List<MaintenanceRecord> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToRecord(rs));
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all maintenance records: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasActiveMaintenance(String vehicleId, LocalDate startDate, LocalDate endDate) {
        if (vehicleId == null || startDate == null || endDate == null) {
            return false;
        }

        String sql = """
                SELECT 1 FROM maintenance
                WHERE vehicle_id = ?
                  AND is_completed = FALSE
                  AND service_date >= ?
                  AND service_date <= ?
                """;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check active maintenance for vehicle '" + vehicleId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String recordId) {
        if (recordId == null) {
            return false;
        }

        String sql = "DELETE FROM maintenance WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, recordId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete maintenance record with ID '" + recordId + "': " + e.getMessage(), e);
        }
    }

    private MaintenanceRecord mapRowToRecord(ResultSet rs) throws SQLException {
        Date serviceDate = rs.getDate("service_date");
        return new MaintenanceRecord(
                rs.getString("id"),
                rs.getString("vehicle_id"),
                serviceDate != null ? serviceDate.toLocalDate() : null,
                rs.getString("description"),
                rs.getBigDecimal("cost"),
                rs.getBoolean("is_completed")
        );
    }
}

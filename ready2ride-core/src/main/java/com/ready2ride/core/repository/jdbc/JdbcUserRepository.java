package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL JDBC-backed implementation of {@link UserRepository}.
 * Provides persistent user account management with prepared statements and robust error handling.
 */
public class JdbcUserRepository implements UserRepository {

    private final DatabaseConnectionManager connectionManager;

    public JdbcUserRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "User must not be null");

        String checkSql = "SELECT 1 FROM users WHERE id = ?";
        String updateSql = """
                UPDATE users
                SET full_name = ?, email = ?, phone_number = ?, password_hash = ?,
                    role = ?, driving_license_number = ?, is_active = ?
                WHERE id = ?
                """;
        String insertSql = """
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash,
                    role, driving_license_number, is_active, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionManager.getConnection()) {
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, user.getFullName());
                    updateStmt.setString(2, user.getEmail().trim().toLowerCase());
                    updateStmt.setString(3, user.getPhoneNumber());
                    updateStmt.setString(4, user.getPasswordHash());
                    updateStmt.setString(5, user.getRole().name());
                    if (user.getDrivingLicenseNumber() != null) {
                        updateStmt.setString(6, user.getDrivingLicenseNumber());
                    } else {
                        updateStmt.setNull(6, Types.VARCHAR);
                    }
                    updateStmt.setBoolean(7, user.isActive());
                    updateStmt.setString(8, user.getId());
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, user.getId());
                    insertStmt.setString(2, user.getFullName());
                    insertStmt.setString(3, user.getEmail().trim().toLowerCase());
                    insertStmt.setString(4, user.getPhoneNumber());
                    insertStmt.setString(5, user.getPasswordHash());
                    insertStmt.setString(6, user.getRole().name());
                    if (user.getDrivingLicenseNumber() != null) {
                        insertStmt.setString(7, user.getDrivingLicenseNumber());
                    } else {
                        insertStmt.setNull(7, Types.VARCHAR);
                    }
                    insertStmt.setBoolean(8, user.isActive());
                    insertStmt.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
                    insertStmt.executeUpdate();
                }
            }

            return user;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DataAccessException("Unique constraint violation: A user with email '" + user.getEmail() + "' already exists.", e);
            }
            throw new DataAccessException("Failed to persist user with ID '" + user.getId() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query user by ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query user by email '" + email + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all users: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findByRole(UserRole role) {
        if (role == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        List<User> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query users by role '" + role + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check email existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check user existence for ID '" + id + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }

        // Soft-delete to preserve referential integrity with bookings
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ? AND is_active = TRUE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to deactivate user with ID '" + id + "': " + e.getMessage(), e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new User(
                rs.getString("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role")),
                rs.getString("driving_license_number"),
                rs.getBoolean("is_active"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}

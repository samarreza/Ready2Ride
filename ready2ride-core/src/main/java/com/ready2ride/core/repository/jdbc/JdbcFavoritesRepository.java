package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.repository.FavoritesRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PostgreSQL JDBC implementation of {@link FavoritesRepository}.
 * Automatically ensures the {@code user_favorites} table exists upon construction.
 */
public class JdbcFavoritesRepository implements FavoritesRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcFavoritesRepository.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public JdbcFavoritesRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
        initTableIfNotExists();
    }

    private void initTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_favorites (
                user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                vehicle_id VARCHAR(50) NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (user_id, vehicle_id)
            );
            CREATE INDEX IF NOT EXISTS idx_user_favorites_user ON user_favorites(user_id);
            """;
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Note during user_favorites table init: " + e.getMessage());
        }
    }

    @Override
    public void addFavorite(String userId, String vehicleId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");

        String sql = "INSERT INTO user_favorites (user_id, vehicle_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, vehicleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add favorite vehicle '" + vehicleId + "' for user '" + userId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void removeFavorite(String userId, String vehicleId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");

        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND vehicle_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, vehicleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove favorite vehicle '" + vehicleId + "' for user '" + userId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isFavorite(String userId, String vehicleId) {
        if (userId == null || vehicleId == null) {
            return false;
        }
        String sql = "SELECT 1 FROM user_favorites WHERE user_id = ? AND vehicle_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking favorite: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Set<String> getFavoriteVehicleIds(String userId) {
        Set<String> favorites = new HashSet<>();
        if (userId == null) {
            return favorites;
        }
        String sql = "SELECT vehicle_id FROM user_favorites WHERE user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favorites.add(rs.getString("vehicle_id"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load favorite vehicle IDs for user '" + userId + "': " + e.getMessage(), e);
        }
        return favorites;
    }
}

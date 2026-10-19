package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.exception.InvalidRentalDateException;
import com.ready2ride.core.exception.VehicleNotAvailableException;
import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.repository.BookingRepository;

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
 * PostgreSQL JDBC-backed implementation of {@link BookingRepository}.
 * Employs row-level pessimistic locking ({@code FOR UPDATE}) and atomic transaction
 * boundaries to prevent double-booking race conditions during simultaneous requests.
 */
public class JdbcBookingRepository implements BookingRepository {

    private final DatabaseConnectionManager connectionManager;

    public JdbcBookingRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    @Override
    public Booking save(Booking booking) {
        Objects.requireNonNull(booking, "Booking must not be null");

        // Reject invalid dates before database operations
        if (!booking.getReturnDateTime().isAfter(booking.getPickupDateTime())) {
            throw new InvalidRentalDateException(String.format(
                    "Return date/time (%s) must be strictly later than pickup date/time (%s).",
                    booking.getReturnDateTime(), booking.getPickupDateTime()));
        }

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Check if the booking already exists
                boolean exists = false;
                String checkBookingSql = "SELECT id FROM bookings WHERE id = ? FOR UPDATE";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkBookingSql)) {
                    checkStmt.setString(1, booking.getBookingId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        exists = rs.next();
                    }
                }

                // 2. Lock the vehicle row to serialize concurrent booking attempts for this vehicle
                String lockVehicleSql = "SELECT id FROM vehicles WHERE id = ? FOR UPDATE";
                try (PreparedStatement lockStmt = conn.prepareStatement(lockVehicleSql)) {
                    lockStmt.setString(1, booking.getVehicleId());
                    try (ResultSet rs = lockStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new VehicleNotFoundException("Vehicle with ID '" + booking.getVehicleId() + "' was not found.");
                        }
                    }
                }

                // 3. If this booking is active, verify that no overlapping active reservations exist
                if (booking.getStatus().blocksAvailability()) {
                    checkOverlappingReservations(conn, booking, exists);
                }

                // 4. Perform Insert or Update
                if (exists) {
                    String updateSql = """
                            UPDATE bookings
                            SET customer_id = ?, vehicle_id = ?, pickup_date_time = ?,
                                return_date_time = ?, rental_amount = ?, status = ?
                            WHERE id = ?
                            """;
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, booking.getCustomerId());
                        updateStmt.setString(2, booking.getVehicleId());
                        updateStmt.setTimestamp(3, Timestamp.valueOf(booking.getPickupDateTime()));
                        updateStmt.setTimestamp(4, Timestamp.valueOf(booking.getReturnDateTime()));
                        updateStmt.setBigDecimal(5, booking.getRentalAmount());
                        updateStmt.setString(6, booking.getStatus().name());
                        updateStmt.setString(7, booking.getBookingId());
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertSql = """
                            INSERT INTO bookings (
                                id, customer_id, vehicle_id, pickup_date_time,
                                return_date_time, rental_amount, status
                            ) VALUES (?, ?, ?, ?, ?, ?, ?)
                            """;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, booking.getBookingId());
                        insertStmt.setString(2, booking.getCustomerId());
                        insertStmt.setString(3, booking.getVehicleId());
                        insertStmt.setTimestamp(4, Timestamp.valueOf(booking.getPickupDateTime()));
                        insertStmt.setTimestamp(5, Timestamp.valueOf(booking.getReturnDateTime()));
                        insertStmt.setBigDecimal(6, booking.getRentalAmount());
                        insertStmt.setString(7, booking.getStatus().name());
                        insertStmt.executeUpdate();
                    }
                }

                conn.commit();
                return booking;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                SQLException sqle = (SQLException) e;
                // PostgreSQL error code 23514 is CHECK_VIOLATION
                if ("23514".equals(sqle.getSQLState())) {
                    throw new InvalidRentalDateException("Return date/time must be strictly after pickup date/time (database constraint).", sqle);
                }
                throw new DataAccessException("Failed to persist booking with ID '" + booking.getBookingId() + "': " + sqle.getMessage(), sqle);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database transaction error: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        if (bookingId == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBooking(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query booking by ID '" + bookingId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findAll() {
        String sql = "SELECT * FROM bookings ORDER BY pickup_date_time DESC";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all bookings: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM bookings WHERE vehicle_id = ? ORDER BY pickup_date_time";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query bookings for vehicle '" + vehicleId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findByCustomerId(String customerId) {
        if (customerId == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY pickup_date_time DESC";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query bookings for customer '" + customerId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String bookingId) {
        if (bookingId == null) {
            return false;
        }

        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bookingId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete booking with ID '" + bookingId + "': " + e.getMessage(), e);
        }
    }

    private void checkOverlappingReservations(Connection conn, Booking booking, boolean isUpdate) throws SQLException {
        // Condition: requestedPickup < existingReturn AND requestedReturn > existingPickup
        // In SQL terms: pickup_date_time < requestedReturn AND return_date_time > requestedPickup
        String overlapSql = """
                SELECT id, pickup_date_time, return_date_time
                FROM bookings
                WHERE vehicle_id = ?
                  AND status IN ('PENDING', 'CONFIRMED')
                  AND pickup_date_time < ?
                  AND return_date_time > ?
                """ + (isUpdate ? " AND id <> ?" : "") + " FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(overlapSql)) {
            stmt.setString(1, booking.getVehicleId());
            stmt.setTimestamp(2, Timestamp.valueOf(booking.getReturnDateTime()));
            stmt.setTimestamp(3, Timestamp.valueOf(booking.getPickupDateTime()));
            if (isUpdate) {
                stmt.setString(4, booking.getBookingId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String conflictingId = rs.getString("id");
                    LocalDateTime conflictStart = rs.getTimestamp("pickup_date_time").toLocalDateTime();
                    LocalDateTime conflictEnd = rs.getTimestamp("return_date_time").toLocalDateTime();

                    throw new VehicleNotAvailableException(String.format(
                            "Vehicle '%s' is not available from %s to %s. Conflicts with active booking '%s' [%s to %s].",
                            booking.getVehicleId(),
                            booking.getPickupDateTime(),
                            booking.getReturnDateTime(),
                            conflictingId,
                            conflictStart,
                            conflictEnd
                    ));
                }
            }
        }
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getString("id"),
                rs.getString("customer_id"),
                rs.getString("vehicle_id"),
                rs.getTimestamp("pickup_date_time").toLocalDateTime(),
                rs.getTimestamp("return_date_time").toLocalDateTime(),
                rs.getBigDecimal("rental_amount"),
                BookingStatus.valueOf(rs.getString("status"))
        );
    }
}

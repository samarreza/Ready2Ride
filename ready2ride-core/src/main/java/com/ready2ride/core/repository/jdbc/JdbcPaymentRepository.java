package com.ready2ride.core.repository.jdbc;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.DataAccessException;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.PaymentMethod;
import com.ready2ride.core.model.PaymentStatus;
import com.ready2ride.core.repository.PaymentRepository;

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
 * PostgreSQL JDBC-backed implementation of {@link PaymentRepository}.
 */
public class JdbcPaymentRepository implements PaymentRepository {

    private final DatabaseConnectionManager connectionManager;

    public JdbcPaymentRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
    }

    @Override
    public Payment save(Payment payment) {
        Objects.requireNonNull(payment, "Payment must not be null");

        String checkSql = "SELECT 1 FROM payments WHERE id = ?";
        String updateSql = """
                UPDATE payments
                SET booking_id = ?, amount = ?, payment_date_time = ?,
                    payment_method = ?, status = ?, transaction_reference = ?
                WHERE id = ?
                """;
        String insertSql = """
                INSERT INTO payments (
                    id, booking_id, amount, payment_date_time,
                    payment_method, status, transaction_reference
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionManager.getConnection()) {
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, payment.getPaymentId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, payment.getBookingId());
                    updateStmt.setBigDecimal(2, payment.getAmount());
                    updateStmt.setTimestamp(3, Timestamp.valueOf(payment.getPaymentDateTime()));
                    updateStmt.setString(4, payment.getPaymentMethod().name());
                    updateStmt.setString(5, payment.getStatus().name());
                    if (payment.getTransactionReference() != null) {
                        updateStmt.setString(6, payment.getTransactionReference());
                    } else {
                        updateStmt.setNull(6, Types.VARCHAR);
                    }
                    updateStmt.setString(7, payment.getPaymentId());
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, payment.getPaymentId());
                    insertStmt.setString(2, payment.getBookingId());
                    insertStmt.setBigDecimal(3, payment.getAmount());
                    insertStmt.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDateTime()));
                    insertStmt.setString(5, payment.getPaymentMethod().name());
                    insertStmt.setString(6, payment.getStatus().name());
                    if (payment.getTransactionReference() != null) {
                        insertStmt.setString(7, payment.getTransactionReference());
                    } else {
                        insertStmt.setNull(7, Types.VARCHAR);
                    }
                    insertStmt.executeUpdate();
                }
            }

            return payment;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to persist payment with ID '" + payment.getPaymentId() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        if (paymentId == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM payments WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPayment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query payment by ID '" + paymentId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Payment> findByBookingId(String bookingId) {
        if (bookingId == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_date_time DESC";
        List<Payment> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToPayment(rs));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query payments for booking '" + bookingId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY payment_date_time DESC";
        List<Payment> list = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToPayment(rs));
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all payments: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String paymentId) {
        if (paymentId == null) {
            return false;
        }

        String sql = "DELETE FROM payments WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paymentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete payment with ID '" + paymentId + "': " + e.getMessage(), e);
        }
    }

    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getString("id"),
                rs.getString("booking_id"),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("payment_date_time").toLocalDateTime(),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                PaymentStatus.valueOf(rs.getString("status")),
                rs.getString("transaction_reference")
        );
    }
}

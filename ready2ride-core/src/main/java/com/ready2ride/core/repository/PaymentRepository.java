package com.ready2ride.core.repository;

import com.ready2ride.core.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for {@link Payment} transaction records.
 */
public interface PaymentRepository {

    /**
     * Saves or updates a payment transaction.
     *
     * @param payment payment entity to persist
     * @return persisted payment
     */
    Payment save(Payment payment);

    /**
     * Finds a payment record by its unique identifier.
     *
     * @param paymentId payment ID
     * @return {@link Optional} containing payment if found, empty otherwise
     */
    Optional<Payment> findById(String paymentId);

    /**
     * Retrieves all payments linked to a specific booking.
     *
     * @param bookingId booking ID
     * @return list of payments for the booking
     */
    List<Payment> findByBookingId(String bookingId);

    /**
     * Retrieves all payment records.
     *
     * @return unmodifiable list of all payments
     */
    List<Payment> findAll();

    /**
     * Deletes a payment by identifier.
     *
     * @param paymentId payment ID
     * @return true if deleted; false otherwise
     */
    boolean deleteById(String paymentId);
}

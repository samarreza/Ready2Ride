package com.ready2ride.core.service;

import com.ready2ride.core.exception.BookingNotFoundException;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.PaymentMethod;
import com.ready2ride.core.model.PaymentStatus;
import com.ready2ride.core.repository.BookingRepository;
import com.ready2ride.core.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service orchestrating payment transactions, demo/simulated checkout validation,
 * and state synchronization with booking reservations.
 * <p>
 * Complies with strict PCI-DSS safety standards: Never persists or accepts raw card
 * numbers, CVVs, or online banking passwords into the database.
 * </p>
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "PaymentRepository must not be null");
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "BookingRepository must not be null");
    }

    /**
     * Processes a simulated payment for a confirmed or pending booking.
     * Persists the transaction audit record to PostgreSQL and ensures the booking is marked CONFIRMED.
     *
     * @param bookingId     associated booking identifier
     * @param amount        payment transaction amount
     * @param paymentMethod selected payment method (CARD, UPI, NET_BANKING, etc.)
     * @return persisted {@link Payment} record
     * @throws BookingNotFoundException if bookingId does not match an existing booking
     * @throws IllegalArgumentException if amount is non-positive
     */
    public Payment processPayment(String bookingId, BigDecimal amount, PaymentMethod paymentMethod) {
        Objects.requireNonNull(bookingId, "Booking ID must not be null");
        Objects.requireNonNull(amount, "Payment amount must not be null");
        Objects.requireNonNull(paymentMethod, "Payment method must not be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be strictly greater than zero.");
        }

        // Verify that the booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID '" + bookingId + "' not found."));

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String txnRef = "TXN-R2R-" + System.currentTimeMillis() % 10000000;

        Payment payment = new Payment(
                paymentId,
                bookingId,
                amount,
                LocalDateTime.now(),
                paymentMethod,
                PaymentStatus.COMPLETED,
                txnRef
        );

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status if needed
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        return savedPayment;
    }

    /**
     * Retrieves all payments recorded for a specific booking.
     *
     * @param bookingId booking identifier
     * @return list of payments linked to this booking
     */
    public List<Payment> getPaymentsByBooking(String bookingId) {
        Objects.requireNonNull(bookingId, "Booking ID must not be null");
        return paymentRepository.findByBookingId(bookingId);
    }

    /**
     * Retrieves all payments across the entire system.
     *
     * @return list of all payment records
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Retrieves a single payment record by its identifier.
     *
     * @param paymentId payment identifier
     * @return Optional containing payment if found
     */
    public Optional<Payment> getPaymentById(String paymentId) {
        Objects.requireNonNull(paymentId, "Payment ID must not be null");
        return paymentRepository.findById(paymentId);
    }
}

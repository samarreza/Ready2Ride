package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a payment transaction linked to a booking.
 */
public class Payment {

    private final String paymentId;
    private final String bookingId;
    private BigDecimal amount;
    private LocalDateTime paymentDateTime;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;

    public Payment(String paymentId, String bookingId, BigDecimal amount,
                   LocalDateTime paymentDateTime, PaymentMethod paymentMethod,
                   PaymentStatus status, String transactionReference) {
        this.paymentId = Objects.requireNonNull(paymentId, "Payment ID must not be null");
        this.bookingId = Objects.requireNonNull(bookingId, "Booking ID must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.paymentDateTime = Objects.requireNonNull(paymentDateTime, "Payment date/time must not be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method must not be null");
        this.status = Objects.requireNonNull(status, "Payment status must not be null");
        this.transactionReference = transactionReference;
    }

    public Payment(String paymentId, String bookingId, BigDecimal amount,
                   LocalDateTime paymentDateTime, PaymentMethod paymentMethod, PaymentStatus status) {
        this(paymentId, bookingId, amount, paymentDateTime, paymentMethod, status, null);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }

    public void setPaymentDateTime(LocalDateTime paymentDateTime) {
        this.paymentDateTime = Objects.requireNonNull(paymentDateTime, "Payment date/time must not be null");
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method must not be null");
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = Objects.requireNonNull(status, "Payment status must not be null");
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment other)) return false;
        return Objects.equals(paymentId, other.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return String.format("Payment [id=%s, booking=%s, amount=%s, method=%s, status=%s, ref=%s]",
                paymentId, bookingId, amount, paymentMethod, status, transactionReference);
    }
}

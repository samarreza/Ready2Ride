package com.ready2ride.core.repository;

import com.ready2ride.core.model.Payment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link PaymentRepository} for unit testing.
 */
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> storage = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        Objects.requireNonNull(payment, "Payment must not be null");
        storage.put(payment.getPaymentId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        if (paymentId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(paymentId));
    }

    @Override
    public List<Payment> findByBookingId(String bookingId) {
        if (bookingId == null) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(p -> bookingId.equals(p.getBookingId()))
                .toList();
    }

    @Override
    public List<Payment> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public boolean deleteById(String paymentId) {
        return paymentId != null && storage.remove(paymentId) != null;
    }

    public void clear() {
        storage.clear();
    }
}

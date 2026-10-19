package com.ready2ride.core.repository;

import com.ready2ride.core.model.Booking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link BookingRepository} using an {@link ArrayList}
 * to demonstrate Java List collections for maintaining, traversing, and searching booking records.
 */
public class InMemoryBookingRepository implements BookingRepository {

    private final List<Booking> bookings = new ArrayList<>();

    @Override
    public Booking save(Booking booking) {
        Objects.requireNonNull(booking, "Booking to save must not be null");

        // If booking already exists, replace it to support update semantics
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId().equals(booking.getBookingId())) {
                bookings.set(i, booking);
                return booking;
            }
        }

        bookings.add(booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        if (bookingId == null) {
            return Optional.empty();
        }
        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(bookingId)) {
                return Optional.of(booking);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(bookings));
    }

    @Override
    public List<Booking> findByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return Collections.emptyList();
        }
        return bookings.stream()
                .filter(b -> vehicleId.equals(b.getVehicleId()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<Booking> findByCustomerId(String customerId) {
        if (customerId == null) {
            return Collections.emptyList();
        }
        return bookings.stream()
                .filter(b -> customerId.equals(b.getCustomerId()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public boolean deleteById(String bookingId) {
        if (bookingId == null) {
            return false;
        }
        return bookings.removeIf(b -> b.getBookingId().equals(bookingId));
    }

    /**
     * Clears all stored bookings (useful for isolated testing).
     */
    public void clear() {
        bookings.clear();
    }
}

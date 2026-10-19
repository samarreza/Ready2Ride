package com.ready2ride.core.repository;

import com.ready2ride.core.model.Booking;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface abstraction for booking entity operations.
 * Designed to be implemented in-memory for Phase 1 and swapped with JDBC/PostgreSQL in Phase 2.
 */
public interface BookingRepository {

    /**
     * Saves or updates a booking record in the repository.
     *
     * @param booking booking entity to persist
     * @return the persisted booking
     */
    Booking save(Booking booking);

    /**
     * Finds a booking by its unique identifier.
     *
     * @param bookingId booking ID
     * @return {@link Optional} containing booking if found, empty otherwise
     */
    Optional<Booking> findById(String bookingId);

    /**
     * Retrieves all bookings stored in the repository.
     *
     * @return unmodifiable list of all bookings
     */
    List<Booking> findAll();

    /**
     * Retrieves all bookings for a specific vehicle.
     *
     * @param vehicleId vehicle ID
     * @return list of bookings for the vehicle
     */
    List<Booking> findByVehicleId(String vehicleId);

    /**
     * Retrieves all bookings for a specific customer.
     *
     * @param customerId customer ID
     * @return list of bookings for the customer
     */
    List<Booking> findByCustomerId(String customerId);

    /**
     * Deletes a booking by its unique identifier.
     *
     * @param bookingId booking ID
     * @return true if removed; false otherwise
     */
    boolean deleteById(String bookingId);
}

package com.ready2ride.core.service;

import com.ready2ride.core.exception.BookingNotFoundException;
import com.ready2ride.core.exception.InvalidRentalDateException;
import com.ready2ride.core.exception.VehicleNotAvailableException;
import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.BookingRepository;
import com.ready2ride.core.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service class orchestrating rental booking reservations and availability enforcement.
 * Demonstrates runtime polymorphism, collections processing, and custom exception handling.
 */
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRepository maintenanceRepository;

    public BookingService(BookingRepository bookingRepository, VehicleRepository vehicleRepository) {
        this(bookingRepository, vehicleRepository, null);
    }

    public BookingService(BookingRepository bookingRepository, VehicleRepository vehicleRepository,
                          MaintenanceRepository maintenanceRepository) {
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "BookingRepository must not be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository must not be null");
        this.maintenanceRepository = maintenanceRepository;
    }

    /**
     * Creates and confirms a new rental booking for a selected vehicle.
     * Generates a unique booking identifier automatically.
     *
     * @param customerId     identifier of the renting customer
     * @param vehicleId      identifier of the vehicle to book
     * @param pickupDateTime requested pickup date and time
     * @param returnDateTime requested return date and time
     * @return created and persisted {@link Booking}
     * @throws InvalidRentalDateException   if dates are null or return is not strictly after pickup
     * @throws VehicleNotFoundException     if vehicleId does not match an existing vehicle
     * @throws VehicleNotAvailableException if vehicle has an active overlapping reservation
     */
    public Booking createBooking(String customerId, String vehicleId,
                                 LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        String generatedId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return createBooking(generatedId, customerId, vehicleId, pickupDateTime, returnDateTime);
    }

    /**
     * Creates and confirms a new rental booking with an explicit booking ID.
     *
     * @param bookingId      custom unique booking identifier
     * @param customerId     identifier of the renting customer
     * @param vehicleId      identifier of the vehicle to book
     * @param pickupDateTime requested pickup date and time
     * @param returnDateTime requested return date and time
     * @return created and persisted {@link Booking}
     * @throws InvalidRentalDateException   if dates are null or return is not strictly after pickup
     * @throws VehicleNotFoundException     if vehicleId does not match an existing vehicle
     * @throws VehicleNotAvailableException if vehicle has an active overlapping reservation
     */
    public Booking createBooking(String bookingId, String customerId, String vehicleId,
                                 LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        Objects.requireNonNull(bookingId, "Booking ID must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");

        // 1. Validate date/time range
        validateDates(pickupDateTime, returnDateTime);

        // 2. Locate vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID '" + vehicleId + "' was not found."));

        if (!vehicle.isActive()) {
            throw new VehicleNotAvailableException("Vehicle with ID '" + vehicleId + "' is inactive and cannot be booked.");
        }

        if (maintenanceRepository != null &&
                maintenanceRepository.hasActiveMaintenance(vehicleId, pickupDateTime.toLocalDate(), returnDateTime.toLocalDate())) {
            throw new VehicleNotAvailableException(String.format(
                    "Vehicle '%s' is scheduled for maintenance between %s and %s and cannot be booked.",
                    vehicleId, pickupDateTime.toLocalDate(), returnDateTime.toLocalDate()));
        }

        // 3. Verify vehicle availability against active overlapping reservations
        checkAvailability(vehicleId, pickupDateTime, returnDateTime);

        // 4. Calculate rental cost polymorphically via Vehicle reference
        BigDecimal rentalAmount = vehicle.calculateRentalCost(pickupDateTime, returnDateTime);

        // 5. Build and persist booking
        Booking booking = new Booking(
                bookingId,
                customerId,
                vehicleId,
                pickupDateTime,
                returnDateTime,
                rentalAmount,
                BookingStatus.CONFIRMED
        );

        return bookingRepository.save(booking);
    }

    /**
     * Checks if a vehicle is currently available for the requested time range.
     *
     * @param vehicleId      vehicle identifier
     * @param pickupDateTime requested pickup date/time
     * @param returnDateTime requested return date/time
     * @return true if vehicle exists, is active, has no incomplete maintenance and has no overlapping active bookings; false otherwise
     */
    public boolean isVehicleAvailable(String vehicleId, LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty() || !vehicleOpt.get().isActive()) {
            return false;
        }
        if (pickupDateTime == null || returnDateTime == null || !returnDateTime.isAfter(pickupDateTime)) {
            return false;
        }
        if (maintenanceRepository != null &&
                maintenanceRepository.hasActiveMaintenance(vehicleId, pickupDateTime.toLocalDate(), returnDateTime.toLocalDate())) {
            return false;
        }

        List<Booking> existingBookings = bookingRepository.findByVehicleId(vehicleId);
        for (Booking existing : existingBookings) {
            if (existing.blocksAvailability() && existing.overlapsWith(pickupDateTime, returnDateTime)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cancels an existing booking. Cancelled bookings will no longer block vehicle availability.
     *
     * @param bookingId booking identifier
     * @return updated booking with CANCELLED status
     * @throws BookingNotFoundException if booking does not exist
     */
    public Booking cancelBooking(String bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Retrieves a booking by its identifier.
     *
     * @param bookingId booking identifier
     * @return matching booking
     * @throws BookingNotFoundException if booking does not exist
     */
    public Booking getBookingById(String bookingId) {
        Objects.requireNonNull(bookingId, "Booking ID must not be null");
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID '" + bookingId + "' was not found."));
    }

    /**
     * Retrieves all bookings in the repository.
     *
     * @return list of all bookings
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Retrieves all bookings for a given vehicle.
     *
     * @param vehicleId vehicle identifier
     * @return list of bookings for this vehicle
     */
    public List<Booking> getBookingsByVehicle(String vehicleId) {
        return bookingRepository.findByVehicleId(vehicleId);
    }

    /**
     * Retrieves all bookings made by a given customer.
     *
     * @param customerId customer identifier
     * @return list of bookings for this customer
     */
    public List<Booking> getBookingsByCustomer(String customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    private void validateDates(LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        if (pickupDateTime == null || returnDateTime == null) {
            throw new InvalidRentalDateException("Pickup date/time and return date/time must not be null.");
        }
        if (!returnDateTime.isAfter(pickupDateTime)) {
            throw new InvalidRentalDateException(String.format(
                    "Return date/time (%s) must be strictly later than pickup date/time (%s).",
                    returnDateTime, pickupDateTime));
        }
    }

    private void checkAvailability(String vehicleId, LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        List<Booking> vehicleBookings = bookingRepository.findByVehicleId(vehicleId);
        for (Booking existing : vehicleBookings) {
            // Overlapping condition: requestedPickup < existingReturn AND requestedReturn > existingPickup
            if (existing.blocksAvailability() && existing.overlapsWith(pickupDateTime, returnDateTime)) {
                throw new VehicleNotAvailableException(String.format(
                        "Vehicle '%s' is not available from %s to %s. Conflicts with active booking '%s' [%s to %s].",
                        vehicleId, pickupDateTime, returnDateTime,
                        existing.getBookingId(), existing.getPickupDateTime(), existing.getReturnDateTime()));
            }
        }
    }
}

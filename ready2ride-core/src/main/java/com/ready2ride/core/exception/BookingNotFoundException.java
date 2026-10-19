package com.ready2ride.core.exception;

/**
 * Thrown when a requested booking record cannot be found in the system.
 */
public class BookingNotFoundException extends Ready2RideException {

    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.ready2ride.core.exception;

/**
 * Thrown when rental pickup and return date/time values are invalid,
 * such as when the return time is not strictly after the pickup time or dates are null.
 */
public class InvalidRentalDateException extends Ready2RideException {

    public InvalidRentalDateException(String message) {
        super(message);
    }

    public InvalidRentalDateException(String message, Throwable cause) {
        super(message, cause);
    }
}

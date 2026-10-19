package com.ready2ride.core.exception;

/**
 * Base unchecked exception for all Ready2Ride domain and business logic errors.
 */
public class Ready2RideException extends RuntimeException {

    public Ready2RideException(String message) {
        super(message);
    }

    public Ready2RideException(String message, Throwable cause) {
        super(message, cause);
    }
}

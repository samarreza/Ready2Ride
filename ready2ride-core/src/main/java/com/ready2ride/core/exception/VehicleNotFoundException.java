package com.ready2ride.core.exception;

/**
 * Thrown when a vehicle cannot be found by its identifier.
 */
public class VehicleNotFoundException extends Ready2RideException {

    public VehicleNotFoundException(String message) {
        super(message);
    }

    public VehicleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

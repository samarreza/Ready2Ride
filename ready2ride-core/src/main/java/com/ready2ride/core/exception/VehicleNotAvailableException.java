package com.ready2ride.core.exception;

/**
 * Thrown when a vehicle is unavailable for the requested booking window,
 * such as due to an overlapping reservation or maintenance status.
 */
public class VehicleNotAvailableException extends Ready2RideException {

    public VehicleNotAvailableException(String message) {
        super(message);
    }

    public VehicleNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

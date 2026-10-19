package com.ready2ride.core.exception;

/**
 * Thrown when attempting to register a user with an email or identifier
 * that is already registered in the system.
 */
public class UserAlreadyExistsException extends Ready2RideException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

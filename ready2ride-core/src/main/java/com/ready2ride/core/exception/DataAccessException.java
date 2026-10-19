package com.ready2ride.core.exception;

/**
 * Thrown when an underlying SQL or JDBC data persistence error occurs.
 * Encapsulates lower-level SQLExceptions to insulate business and service layers.
 */
public class DataAccessException extends Ready2RideException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

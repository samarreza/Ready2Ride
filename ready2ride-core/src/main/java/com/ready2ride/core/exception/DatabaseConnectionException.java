package com.ready2ride.core.exception;

/**
 * Thrown when an error occurs establishing or validating a database connection.
 * Formatted to prevent leaking credentials or confidential connection parameters.
 */
public class DatabaseConnectionException extends Ready2RideException {

    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

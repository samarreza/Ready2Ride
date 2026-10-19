package com.ready2ride.core.exception;

/**
 * Thrown when user authentication fails due to invalid credentials,
 * inactive account status, or insufficient role authorizations.
 */
public class AuthenticationException extends Ready2RideException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

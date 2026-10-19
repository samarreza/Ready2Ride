package com.ready2ride.core.database;

import java.util.Objects;

/**
 * Encapsulates database connection configuration settings for Ready2Ride.
 * Loads connection parameters from environment variables with safe defaults for local development.
 * Never prints or leaks passwords in logs or string representations.
 */
public class DatabaseConfig {

    public static final String ENV_DB_URL = "READY2RIDE_DB_URL";
    public static final String ENV_DB_USER = "READY2RIDE_DB_USER";
    public static final String ENV_DB_PASSWORD = "READY2RIDE_DB_PASSWORD";

    public static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/ready2ride";

    private final String url;
    private final String user;
    private final String password;

    /**
     * Constructs a {@link DatabaseConfig} instance with explicit parameters.
     *
     * @param url      PostgreSQL JDBC URL (e.g. {@code jdbc:postgresql://localhost:5432/ready2ride})
     * @param user     database user name
     * @param password database user password (nullable)
     */
    public DatabaseConfig(String url, String user, String password) {
        this.url = (url != null && !url.isBlank()) ? url.trim() : DEFAULT_DB_URL;
        this.user = (user != null) ? user.trim() : null;
        this.password = password;
    }

    /**
     * Loads database configuration from the system environment variables:
     * <ul>
     *     <li>{@code READY2RIDE_DB_URL} (defaults to {@value #DEFAULT_DB_URL})</li>
     *     <li>{@code READY2RIDE_DB_USER}</li>
     *     <li>{@code READY2RIDE_DB_PASSWORD}</li>
     * </ul>
     *
     * @return initialized {@link DatabaseConfig}
     */
    public static DatabaseConfig fromEnvironment() {
        String envUrl = System.getenv(ENV_DB_URL);
        String envUser = System.getenv(ENV_DB_USER);
        String envPass = System.getenv(ENV_DB_PASSWORD);

        return new DatabaseConfig(envUrl, envUser, envPass);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Checks if minimum required user credentials have been configured.
     *
     * @return true if both user and password are non-null and non-empty
     */
    public boolean hasCredentials() {
        return user != null && !user.isBlank() && password != null && !password.isBlank();
    }

    @Override
    public String toString() {
        // Redacts password to prevent accidental log leakage
        return String.format("DatabaseConfig[url='%s', user='%s', password=%s]",
                url,
                (user != null ? user : "<not set>"),
                (password != null && !password.isEmpty() ? "********" : "<not set>"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseConfig that)) return false;
        return Objects.equals(url, that.url) &&
               Objects.equals(user, that.user) &&
               Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, user, password);
    }
}

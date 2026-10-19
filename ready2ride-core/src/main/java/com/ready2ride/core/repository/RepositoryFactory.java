package com.ready2ride.core.repository;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.repository.jdbc.JdbcBookingRepository;
import com.ready2ride.core.repository.jdbc.JdbcMaintenanceRepository;
import com.ready2ride.core.repository.jdbc.JdbcPaymentRepository;
import com.ready2ride.core.repository.jdbc.JdbcUserRepository;
import com.ready2ride.core.repository.jdbc.JdbcVehicleRepository;

import java.util.Objects;

/**
 * Factory for creating repository instances across different persistence strategies.
 * Provides explicit, non-framework dependency injection support for both in-memory
 * and PostgreSQL JDBC backends.
 */
public final class RepositoryFactory {

    private RepositoryFactory() {
        // Prevent instantiation of static factory
    }

    /**
     * Creates a new in-memory vehicle repository.
     *
     * @return {@link InMemoryVehicleRepository}
     */
    public static VehicleRepository createInMemoryVehicleRepository() {
        return new InMemoryVehicleRepository();
    }

    /**
     * Creates a new in-memory booking repository.
     *
     * @return {@link InMemoryBookingRepository}
     */
    public static BookingRepository createInMemoryBookingRepository() {
        return new InMemoryBookingRepository();
    }

    /**
     * Creates a new in-memory user repository.
     *
     * @return {@link InMemoryUserRepository}
     */
    public static UserRepository createInMemoryUserRepository() {
        return new InMemoryUserRepository();
    }

    /**
     * Creates a new in-memory payment repository.
     *
     * @return {@link InMemoryPaymentRepository}
     */
    public static PaymentRepository createInMemoryPaymentRepository() {
        return new InMemoryPaymentRepository();
    }

    /**
     * Creates a new in-memory maintenance repository.
     *
     * @return {@link InMemoryMaintenanceRepository}
     */
    public static MaintenanceRepository createInMemoryMaintenanceRepository() {
        return new InMemoryMaintenanceRepository();
    }

    /**
     * Creates a new PostgreSQL JDBC vehicle repository.
     *
     * @param connectionManager active database connection manager
     * @return {@link JdbcVehicleRepository}
     */
    public static VehicleRepository createJdbcVehicleRepository(DatabaseConnectionManager connectionManager) {
        Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
        return new JdbcVehicleRepository(connectionManager);
    }

    /**
     * Creates a new PostgreSQL JDBC booking repository.
     *
     * @param connectionManager active database connection manager
     * @return {@link JdbcBookingRepository}
     */
    public static BookingRepository createJdbcBookingRepository(DatabaseConnectionManager connectionManager) {
        Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
        return new JdbcBookingRepository(connectionManager);
    }

    /**
     * Creates a new PostgreSQL JDBC user repository.
     *
     * @param connectionManager active database connection manager
     * @return {@link JdbcUserRepository}
     */
    public static UserRepository createJdbcUserRepository(DatabaseConnectionManager connectionManager) {
        Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
        return new JdbcUserRepository(connectionManager);
    }

    /**
     * Creates a new PostgreSQL JDBC payment repository.
     *
     * @param connectionManager active database connection manager
     * @return {@link JdbcPaymentRepository}
     */
    public static PaymentRepository createJdbcPaymentRepository(DatabaseConnectionManager connectionManager) {
        Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
        return new JdbcPaymentRepository(connectionManager);
    }

    /**
     * Creates a new PostgreSQL JDBC maintenance repository.
     *
     * @param connectionManager active database connection manager
     * @return {@link JdbcMaintenanceRepository}
     */
    public static MaintenanceRepository createJdbcMaintenanceRepository(DatabaseConnectionManager connectionManager) {
        Objects.requireNonNull(connectionManager, "DatabaseConnectionManager must not be null");
        return new JdbcMaintenanceRepository(connectionManager);
    }
}

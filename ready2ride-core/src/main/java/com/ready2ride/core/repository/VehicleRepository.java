package com.ready2ride.core.repository;

import com.ready2ride.core.model.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface abstraction for vehicle entity operations.
 * Designed to be implemented in-memory for Phase 1 and swapped with JDBC/PostgreSQL in Phase 2.
 */
public interface VehicleRepository {

    /**
     * Saves or updates a vehicle in the repository.
     *
     * @param vehicle the vehicle entity to persist
     * @return the persisted vehicle
     */
    Vehicle save(Vehicle vehicle);

    /**
     * Finds a vehicle by its unique identifier.
     *
     * @param id vehicle ID
     * @return {@link Optional} containing vehicle if found, empty otherwise
     */
    Optional<Vehicle> findById(String id);

    /**
     * Retrieves all vehicles stored in the repository.
     *
     * @return unmodifiable list of all vehicles
     */
    List<Vehicle> findAll();

    /**
     * Checks if a vehicle with the given identifier exists.
     *
     * @param id vehicle ID
     * @return true if exists; false otherwise
     */
    boolean existsById(String id);

    /**
     * Deletes a vehicle by its unique identifier.
     *
     * @param id vehicle ID
     * @return true if removed; false otherwise
     */
    boolean deleteById(String id);
}

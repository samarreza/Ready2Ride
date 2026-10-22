package com.ready2ride.core.repository;

import java.util.List;
import java.util.Set;

/**
 * Data access abstraction for persisting customer favorite (saved) vehicles.
 */
public interface FavoritesRepository {

    /**
     * Adds a vehicle to a customer's saved favorites.
     *
     * @param userId    customer identifier
     * @param vehicleId vehicle identifier
     */
    void addFavorite(String userId, String vehicleId);

    /**
     * Removes a vehicle from a customer's saved favorites.
     *
     * @param userId    customer identifier
     * @param vehicleId vehicle identifier
     */
    void removeFavorite(String userId, String vehicleId);

    /**
     * Checks if a vehicle is in a customer's favorites.
     *
     * @param userId    customer identifier
     * @param vehicleId vehicle identifier
     * @return true if saved; false otherwise
     */
    boolean isFavorite(String userId, String vehicleId);

    /**
     * Retrieves all favorite vehicle IDs for a customer.
     *
     * @param userId customer identifier
     * @return set of favorite vehicle identifiers
     */
    Set<String> getFavoriteVehicleIds(String userId);
}

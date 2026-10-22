package com.ready2ride.core.service;

import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.FavoritesRepository;
import com.ready2ride.core.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service managing customer saved/favorite vehicles.
 */
public class FavoritesService {

    private final FavoritesRepository favoritesRepository;
    private final VehicleRepository vehicleRepository;

    public FavoritesService(FavoritesRepository favoritesRepository, VehicleRepository vehicleRepository) {
        this.favoritesRepository = Objects.requireNonNull(favoritesRepository, "FavoritesRepository must not be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository must not be null");
    }

    /**
     * Toggles the favorite status of a vehicle for a user.
     *
     * @param userId    customer identifier
     * @param vehicleId vehicle identifier
     * @return new favorite status (true if added, false if removed)
     */
    public boolean toggleFavorite(String userId, String vehicleId) {
        if (userId == null || vehicleId == null) {
            return false;
        }
        if (favoritesRepository.isFavorite(userId, vehicleId)) {
            favoritesRepository.removeFavorite(userId, vehicleId);
            return false;
        } else {
            favoritesRepository.addFavorite(userId, vehicleId);
            return true;
        }
    }

    public boolean isFavorite(String userId, String vehicleId) {
        if (userId == null || vehicleId == null) {
            return false;
        }
        return favoritesRepository.isFavorite(userId, vehicleId);
    }

    public Set<String> getFavoriteVehicleIds(String userId) {
        return favoritesRepository.getFavoriteVehicleIds(userId);
    }

    public List<Vehicle> getFavoriteVehicles(String userId) {
        List<Vehicle> list = new ArrayList<>();
        if (userId == null) {
            return list;
        }
        Set<String> ids = favoritesRepository.getFavoriteVehicleIds(userId);
        for (String id : ids) {
            Optional<Vehicle> v = vehicleRepository.findById(id);
            v.ifPresent(list::add);
        }
        return list;
    }
}

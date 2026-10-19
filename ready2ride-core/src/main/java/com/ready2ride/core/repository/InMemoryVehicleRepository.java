package com.ready2ride.core.repository;

import com.ready2ride.core.model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory implementation of {@link VehicleRepository} using a {@link HashMap}
 * to demonstrate Java Map collections for O(1) keyed lookups by vehicle ID.
 */
public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<String, Vehicle> vehicleMap = new HashMap<>();

    @Override
    public Vehicle save(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle to save must not be null");
        vehicleMap.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(vehicleMap.get(id));
    }

    @Override
    public List<Vehicle> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(vehicleMap.values()));
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return vehicleMap.containsKey(id);
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return vehicleMap.remove(id) != null;
    }

    /**
     * Clears all stored vehicles (useful for isolated testing).
     */
    public void clear() {
        vehicleMap.clear();
    }
}

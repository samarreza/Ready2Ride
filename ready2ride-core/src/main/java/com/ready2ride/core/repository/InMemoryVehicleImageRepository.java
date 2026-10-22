package com.ready2ride.core.repository;

import com.ready2ride.core.model.VehicleImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decoupled in-memory implementation of {@link VehicleImageRepository} for unit testing.
 */
public class InMemoryVehicleImageRepository implements VehicleImageRepository {

    private final Map<String, VehicleImage> storage = new ConcurrentHashMap<>();

    @Override
    public VehicleImage save(VehicleImage image) {
        Objects.requireNonNull(image, "VehicleImage must not be null");
        storage.put(image.getId(), image);
        return image;
    }

    @Override
    public Optional<VehicleImage> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<VehicleImage> findByVehicleId(String vehicleId) {
        if (vehicleId == null) return Collections.emptyList();
        List<VehicleImage> list = new ArrayList<>();
        for (VehicleImage img : storage.values()) {
            if (vehicleId.equals(img.getVehicleId())) {
                list.add(img);
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Optional<VehicleImage> findPrimaryByVehicleId(String vehicleId) {
        if (vehicleId == null) return Optional.empty();
        VehicleImage fallback = null;
        for (VehicleImage img : storage.values()) {
            if (vehicleId.equals(img.getVehicleId())) {
                if (img.isPrimary()) {
                    return Optional.of(img);
                }
                if (fallback == null) {
                    fallback = img;
                }
            }
        }
        return Optional.ofNullable(fallback);
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    @Override
    public boolean deleteByVehicleId(String vehicleId) {
        if (vehicleId == null) return false;
        return storage.entrySet().removeIf(entry -> vehicleId.equals(entry.getValue().getVehicleId()));
    }
}

package com.ready2ride.core.service;

import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.VehicleRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class orchestrating vehicle catalog operations and fleet management.
 * Depends strictly on the {@link VehicleRepository} abstraction.
 */
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final com.ready2ride.core.repository.VehicleImageRepository imageRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this(vehicleRepository, null);
    }

    public VehicleService(VehicleRepository vehicleRepository, com.ready2ride.core.repository.VehicleImageRepository imageRepository) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository must not be null");
        this.imageRepository = imageRepository;
    }

    /**
     * Registers a new vehicle into the fleet.
     *
     * @param vehicle vehicle entity to register
     * @return saved vehicle
     */
    public Vehicle registerVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");
        return vehicleRepository.save(vehicle);
    }

    /**
     * Retrieves a vehicle by ID.
     *
     * @param vehicleId vehicle identifier
     * @return the matching vehicle
     * @throws VehicleNotFoundException if no vehicle exists with the given ID
     */
    public Vehicle getVehicleById(String vehicleId) {
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID '" + vehicleId + "' was not found."));
    }

    /**
     * Retrieves all registered vehicles in the fleet.
     *
     * @return unmodifiable list of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Retrieves all active vehicles filtered by type classification.
     *
     * @param type vehicle classification (CAR, BIKE, SUV, VAN)
     * @return matching vehicles
     */
    public List<Vehicle> getVehiclesByType(com.ready2ride.core.model.VehicleType type) {
        if (type == null) {
            return getAllVehicles();
        }
        return vehicleRepository.findAll().stream()
                .filter(v -> v.getVehicleType() == type)
                .toList();
    }

    /**
     * Checks if a vehicle exists by ID.
     *
     * @param vehicleId vehicle identifier
     * @return true if vehicle exists
     */
    public boolean vehicleExists(String vehicleId) {
        return vehicleRepository.existsById(vehicleId);
    }

    /**
     * Removes a vehicle from the fleet.
     *
     * @param vehicleId vehicle identifier
     * @return true if removed; false otherwise
     */
    public boolean removeVehicle(String vehicleId) {
        return vehicleRepository.deleteById(vehicleId);
    }

    /**
     * Retrieves the primary display image for a vehicle.
     *
     * @param vehicleId vehicle identifier
     * @return Optional containing the primary vehicle image if stored
     */
    public java.util.Optional<com.ready2ride.core.model.VehicleImage> getPrimaryImage(String vehicleId) {
        if (imageRepository == null || vehicleId == null) {
            return java.util.Optional.empty();
        }
        return imageRepository.findPrimaryByVehicleId(vehicleId);
    }

    public java.util.Optional<com.ready2ride.core.model.VehicleImage> getPrimaryVehicleImage(String vehicleId) {
        return getPrimaryImage(vehicleId);
    }

    /**
     * Retrieves all images associated with a vehicle.
     *
     * @param vehicleId vehicle identifier
     * @return list of vehicle images
     */
    public List<com.ready2ride.core.model.VehicleImage> getVehicleImages(String vehicleId) {
        if (imageRepository == null || vehicleId == null) {
            return Collections.emptyList();
        }
        return imageRepository.findByVehicleId(vehicleId);
    }

    /**
     * Saves or replaces a vehicle image in persistent storage.
     *
     * @param image vehicle image entity
     * @return saved vehicle image
     */
    public com.ready2ride.core.model.VehicleImage saveVehicleImage(com.ready2ride.core.model.VehicleImage image) {
        if (imageRepository == null) {
            throw new IllegalStateException("VehicleImageRepository is not configured in this VehicleService instance.");
        }
        return imageRepository.save(image);
    }

    /**
     * Retrieves all vehicles owned by a specific rental provider.
     *
     * @param providerId rental provider user ID
     * @return unmodifiable list of vehicles belonging to the provider
     */
    public List<Vehicle> getVehiclesByProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Collections.emptyList();
        }
        return vehicleRepository.findByProviderId(providerId.trim());
    }

    /**
     * Registers a new vehicle into the fleet associated with the authenticated rental provider.
     *
     * @param vehicle    vehicle entity
     * @param providerId authenticated rental provider ID
     * @return saved vehicle
     */
    public Vehicle registerProviderVehicle(Vehicle vehicle, String providerId) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");
        Objects.requireNonNull(providerId, "Provider ID must not be null");
        vehicle.setProviderId(providerId.trim());
        return vehicleRepository.save(vehicle);
    }

    /**
     * Updates an existing vehicle with strict provider ownership verification.
     *
     * @param vehicle    vehicle entity with updated attributes
     * @param providerId authenticated rental provider ID
     * @return updated vehicle
     * @throws SecurityException if the vehicle belongs to another provider
     */
    public Vehicle updateProviderVehicle(Vehicle vehicle, String providerId) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");
        Objects.requireNonNull(providerId, "Provider ID must not be null");

        Vehicle existing = getVehicleById(vehicle.getId());
        if (existing.getProviderId() != null && !existing.getProviderId().equals(providerId.trim())) {
            throw new SecurityException("Unauthorized: Cannot modify a vehicle owned by another provider.");
        }
        vehicle.setProviderId(providerId.trim());
        return vehicleRepository.save(vehicle);
    }

    /**
     * Deactivates a vehicle with strict provider ownership verification.
     *
     * @param vehicleId  vehicle identifier
     * @param providerId authenticated rental provider ID
     * @return true if deactivated
     * @throws SecurityException if the vehicle belongs to another provider
     */
    public boolean deactivateProviderVehicle(String vehicleId, String providerId) {
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        Objects.requireNonNull(providerId, "Provider ID must not be null");

        Vehicle existing = getVehicleById(vehicleId);
        if (existing.getProviderId() != null && !existing.getProviderId().equals(providerId.trim())) {
            throw new SecurityException("Unauthorized: Cannot deactivate a vehicle owned by another provider.");
        }
        existing.setActive(false);
        vehicleRepository.save(existing);
        return true;
    }

    /**
     * Persists or replaces an image for a vehicle with strict provider ownership verification.
     *
     * @param image      vehicle image entity
     * @param providerId authenticated rental provider ID
     * @return saved image
     * @throws SecurityException if the vehicle belongs to another provider
     */
    public com.ready2ride.core.model.VehicleImage saveProviderVehicleImage(com.ready2ride.core.model.VehicleImage image, String providerId) {
        Objects.requireNonNull(image, "Image must not be null");
        Objects.requireNonNull(providerId, "Provider ID must not be null");

        Vehicle existing = getVehicleById(image.getVehicleId());
        if (existing.getProviderId() != null && !existing.getProviderId().equals(providerId.trim())) {
            throw new SecurityException("Unauthorized: Cannot upload images for a vehicle owned by another provider.");
        }
        return saveVehicleImage(image);
    }

    public com.ready2ride.core.repository.VehicleRepository getVehicleRepository() {
        return vehicleRepository;
    }

    public com.ready2ride.core.repository.VehicleImageRepository getImageRepository() {
        return imageRepository;
    }
}

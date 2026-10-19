package com.ready2ride.core.service;

import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.VehicleRepository;

import java.util.List;
import java.util.Objects;

/**
 * Service class orchestrating vehicle catalog operations and fleet management.
 * Depends strictly on the {@link VehicleRepository} abstraction.
 */
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository must not be null");
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
}

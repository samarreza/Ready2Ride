package com.ready2ride.core.repository;

import com.ready2ride.core.model.VehicleImage;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for persisting and retrieving binary vehicle image assets.
 */
public interface VehicleImageRepository {

    /**
     * Persists or updates a vehicle image record with raw byte data.
     *
     * @param image vehicle image entity
     * @return persisted entity
     */
    VehicleImage save(VehicleImage image);

    /**
     * Finds an image by its unique ID.
     *
     * @param id image ID
     * @return Optional containing the image entity if found
     */
    Optional<VehicleImage> findById(String id);

    /**
     * Retrieves all images associated with a specific vehicle.
     *
     * @param vehicleId vehicle ID
     * @return list of images for this vehicle
     */
    List<VehicleImage> findByVehicleId(String vehicleId);

    /**
     * Retrieves the primary display image for a specific vehicle.
     *
     * @param vehicleId vehicle ID
     * @return Optional containing the primary vehicle image if present
     */
    Optional<VehicleImage> findPrimaryByVehicleId(String vehicleId);

    /**
     * Deletes an image by its ID.
     *
     * @param id image ID
     * @return true if deleted
     */
    boolean deleteById(String id);

    /**
     * Deletes all images for a given vehicle.
     *
     * @param vehicleId vehicle ID
     * @return true if one or more images were deleted
     */
    boolean deleteByVehicleId(String vehicleId);
}

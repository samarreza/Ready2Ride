package com.ready2ride.core.repository;

import com.ready2ride.core.model.MaintenanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for {@link MaintenanceRecord} vehicle service and repair logs.
 */
public interface MaintenanceRepository {

    /**
     * Saves or updates a maintenance record.
     *
     * @param record maintenance entity to persist
     * @return persisted maintenance record
     */
    MaintenanceRecord save(MaintenanceRecord record);

    /**
     * Finds a maintenance record by identifier.
     *
     * @param recordId record ID
     * @return {@link Optional} containing record if found, empty otherwise
     */
    Optional<MaintenanceRecord> findById(String recordId);

    /**
     * Retrieves all maintenance records for a specific vehicle.
     *
     * @param vehicleId vehicle ID
     * @return list of maintenance records for the vehicle
     */
    List<MaintenanceRecord> findByVehicleId(String vehicleId);

    /**
     * Retrieves all maintenance records in the system.
     *
     * @return unmodifiable list of maintenance records
     */
    List<MaintenanceRecord> findAll();

    /**
     * Checks if a vehicle has incomplete maintenance scheduled during a specific date window.
     *
     * @param vehicleId vehicle ID
     * @param startDate window start date
     * @param endDate   window end date
     * @return true if uncompleted maintenance exists; false otherwise
     */
    boolean hasActiveMaintenance(String vehicleId, LocalDate startDate, LocalDate endDate);

    /**
     * Deletes a maintenance record by identifier.
     *
     * @param recordId record ID
     * @return true if deleted; false otherwise
     */
    boolean deleteById(String recordId);
}

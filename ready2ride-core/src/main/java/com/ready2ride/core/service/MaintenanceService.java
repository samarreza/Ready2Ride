package com.ready2ride.core.service;

import com.ready2ride.core.exception.VehicleNotFoundException;
import com.ready2ride.core.model.MaintenanceRecord;
import com.ready2ride.core.repository.MaintenanceRepository;
import com.ready2ride.core.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing vehicle maintenance logs, inspection schedules, and repair records.
 */
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository) {
        this.maintenanceRepository = Objects.requireNonNull(maintenanceRepository, "MaintenanceRepository must not be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository must not be null");
    }

    /**
     * Schedules a new maintenance inspection or repair record for a vehicle.
     *
     * @param vehicleId   target vehicle ID
     * @param serviceDate scheduled date of service
     * @param description description of maintenance work
     * @param cost        estimated or incurred cost
     * @return persisted {@link MaintenanceRecord}
     */
    public MaintenanceRecord scheduleMaintenance(String vehicleId, LocalDate serviceDate, String description, BigDecimal cost) {
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        Objects.requireNonNull(serviceDate, "Service date must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(cost, "Cost must not be null");

        if (!vehicleRepository.existsById(vehicleId)) {
            throw new VehicleNotFoundException("Vehicle with ID '" + vehicleId + "' does not exist.");
        }

        String recordId = "MNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MaintenanceRecord record = new MaintenanceRecord(recordId, vehicleId, serviceDate, description.trim(), cost, false);
        return maintenanceRepository.save(record);
    }

    /**
     * Updates the completion status of a maintenance record.
     *
     * @param recordId    maintenance record ID
     * @param isCompleted completion state
     * @return updated record
     */
    public MaintenanceRecord updateCompletionStatus(String recordId, boolean isCompleted) {
        MaintenanceRecord record = maintenanceRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record with ID '" + recordId + "' not found."));

        record.setCompleted(isCompleted);
        return maintenanceRepository.save(record);
    }

    /**
     * Retrieves all maintenance records in the system.
     *
     * @return list of maintenance records
     */
    public List<MaintenanceRecord> getAllMaintenance() {
        return maintenanceRepository.findAll();
    }

    /**
     * Retrieves maintenance logs for a specific vehicle.
     *
     * @param vehicleId vehicle identifier
     * @return list of maintenance records for the vehicle
     */
    public List<MaintenanceRecord> getMaintenanceByVehicle(String vehicleId) {
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        return maintenanceRepository.findByVehicleId(vehicleId);
    }

    /**
     * Checks if a vehicle has active uncompleted maintenance scheduled within a date range.
     *
     * @param vehicleId target vehicle ID
     * @param startDate start of window
     * @param endDate   end of window
     * @return true if vehicle is undergoing maintenance
     */
    public boolean hasActiveMaintenance(String vehicleId, LocalDate startDate, LocalDate endDate) {
        return maintenanceRepository.hasActiveMaintenance(vehicleId, startDate, endDate);
    }
}

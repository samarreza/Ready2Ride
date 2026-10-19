package com.ready2ride.core.repository;

import com.ready2ride.core.model.MaintenanceRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link MaintenanceRepository} for unit testing.
 */
public class InMemoryMaintenanceRepository implements MaintenanceRepository {

    private final Map<String, MaintenanceRecord> storage = new ConcurrentHashMap<>();

    @Override
    public MaintenanceRecord save(MaintenanceRecord record) {
        Objects.requireNonNull(record, "MaintenanceRecord must not be null");
        storage.put(record.getRecordId(), record);
        return record;
    }

    @Override
    public Optional<MaintenanceRecord> findById(String recordId) {
        if (recordId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(recordId));
    }

    @Override
    public List<MaintenanceRecord> findByVehicleId(String vehicleId) {
        if (vehicleId == null) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(m -> vehicleId.equals(m.getVehicleId()))
                .toList();
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public boolean hasActiveMaintenance(String vehicleId, LocalDate startDate, LocalDate endDate) {
        if (vehicleId == null || startDate == null || endDate == null) {
            return false;
        }
        return storage.values().stream()
                .filter(m -> vehicleId.equals(m.getVehicleId()))
                .filter(m -> !m.isCompleted())
                .anyMatch(m -> !m.getServiceDate().isBefore(startDate) && !m.getServiceDate().isAfter(endDate));
    }

    @Override
    public boolean deleteById(String recordId) {
        return recordId != null && storage.remove(recordId) != null;
    }

    public void clear() {
        storage.clear();
    }
}

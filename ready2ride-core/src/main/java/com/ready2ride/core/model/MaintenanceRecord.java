package com.ready2ride.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a vehicle service, repair, or inspection maintenance record.
 */
public class MaintenanceRecord {

    private final String recordId;
    private final String vehicleId;
    private LocalDate serviceDate;
    private String description;
    private BigDecimal cost;
    private boolean completed;

    public MaintenanceRecord(String recordId, String vehicleId, LocalDate serviceDate,
                             String description, BigDecimal cost, boolean completed) {
        this.recordId = Objects.requireNonNull(recordId, "Record ID must not be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        this.serviceDate = Objects.requireNonNull(serviceDate, "Service date must not be null");
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.cost = Objects.requireNonNull(cost, "Cost must not be null");
        this.completed = completed;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = Objects.requireNonNull(serviceDate, "Service date must not be null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = Objects.requireNonNull(cost, "Cost must not be null");
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceRecord other)) return false;
        return Objects.equals(recordId, other.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return String.format("MaintenanceRecord [id=%s, vehicle=%s, date=%s, cost=%s, completed=%s]",
                recordId, vehicleId, serviceDate, cost, completed);
    }
}

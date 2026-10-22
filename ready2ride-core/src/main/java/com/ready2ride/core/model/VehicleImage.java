package com.ready2ride.core.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Domain model representing a vehicle image asset stored persistently in PostgreSQL.
 * Holds actual raw image binary bytes (BYTEA) and associated metadata.
 */
public class VehicleImage {

    private final String id;
    private final String vehicleId;
    private String fileName;
    private String contentType;
    private byte[] imageData;
    private boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VehicleImage(String id, String vehicleId, String fileName, String contentType,
                        byte[] imageData, boolean isPrimary) {
        this.id = Objects.requireNonNull(id, "Image ID must not be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        this.fileName = Objects.requireNonNull(fileName, "File name must not be null");
        this.contentType = Objects.requireNonNull(contentType, "Content type must not be null");
        this.imageData = Objects.requireNonNull(imageData, "Image data must not be null");
        this.isPrimary = isPrimary;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public VehicleImage(String id, String vehicleId, String fileName, String contentType,
                        byte[] imageData, boolean isPrimary, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Image ID must not be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        this.fileName = Objects.requireNonNull(fileName, "File name must not be null");
        this.contentType = Objects.requireNonNull(contentType, "Content type must not be null");
        this.imageData = Objects.requireNonNull(imageData, "Image data must not be null");
        this.isPrimary = isPrimary;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = Objects.requireNonNull(fileName, "File name must not be null");
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = Objects.requireNonNull(contentType, "Content type must not be null");
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = Objects.requireNonNull(imageData, "Image data must not be null");
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleImage that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("VehicleImage[id='%s', vehicleId='%s', fileName='%s', contentType='%s', sizeBytes=%d, isPrimary=%s]",
                id, vehicleId, fileName, contentType, (imageData != null ? imageData.length : 0), isPrimary);
    }
}

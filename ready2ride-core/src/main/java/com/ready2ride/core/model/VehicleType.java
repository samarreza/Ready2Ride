package com.ready2ride.core.model;

/**
 * Enumeration of supported vehicle classifications.
 */
public enum VehicleType {
    CAR("Car"),
    BIKE("Bike"),
    SUV("SUV"),
    VAN("Van");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

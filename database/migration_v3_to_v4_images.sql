-- ==============================================================================
-- Ready2Ride: Vehicle Rental Management System
-- Database Migration Script (v3 to v4: Vehicle Images & Spec Enhancements)
-- Non-destructive, idempotent migration for storing binary vehicle images
-- and extended fleet specifications in PostgreSQL.
-- ==============================================================================

-- 1. Extend vehicles table with optional specs (bags capacity and mileage)
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS luggage_capacity_bags INT NULL;
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS mileage_kmpl INT NULL;

-- 2. Vehicle Images Table (Binary image storage via BYTEA)
CREATE TABLE IF NOT EXISTS vehicle_images (
    id VARCHAR(50) PRIMARY KEY,
    vehicle_id VARCHAR(50) NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    image_data BYTEA NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast image retrieval by vehicle ID
CREATE INDEX IF NOT EXISTS idx_vehicle_images_vehicle ON vehicle_images(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_images_primary ON vehicle_images(vehicle_id, is_primary);

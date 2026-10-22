-- ==============================================================================
-- Ready2Ride: Vehicle Rental Management System
-- Migration v4 to v5: User Favorites (Saved Vehicles)
-- Non-destructive, idempotent migration to persist customer favorite vehicles
-- ==============================================================================

CREATE TABLE IF NOT EXISTS user_favorites (
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_id VARCHAR(50) NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, vehicle_id)
);

CREATE INDEX IF NOT EXISTS idx_user_favorites_user ON user_favorites(user_id);

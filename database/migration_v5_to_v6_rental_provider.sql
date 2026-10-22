-- ==============================================================================
-- Ready2Ride: Vehicle Rental Management System
-- Migration v5 to v6: Rental Provider Role & Vehicle Ownership
-- Non-destructive, idempotent migration
-- ==============================================================================

-- 1. Update users table role check constraint to support RENTAL_PROVIDER
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('CUSTOMER', 'RENTAL_PROVIDER', 'ADMIN'));

-- 2. Add provider_id column to vehicles referencing users(id)
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS provider_id VARCHAR(50) NULL REFERENCES users(id) ON DELETE SET NULL;

-- 3. Create index for fast provider vehicle filtering
CREATE INDEX IF NOT EXISTS idx_vehicles_provider ON vehicles(provider_id);

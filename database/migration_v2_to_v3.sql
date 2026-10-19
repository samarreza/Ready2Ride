-- ==============================================================================
-- Ready2Ride: Vehicle Rental Management System
-- Database Migration Script (v2 to v3)
-- Non-destructive, idempotent migration for account persistence and fleet tables.
-- ==============================================================================

-- 1. Ensure Users Table exists with role and status constraints
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(25) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN')),
    driving_license_number VARCHAR(50) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 2. Ensure Vehicles Table exists with polymorphic subtype attributes
CREATE TABLE IF NOT EXISTS vehicles (
    id VARCHAR(50) PRIMARY KEY,
    vehicle_type VARCHAR(20) NOT NULL CHECK (vehicle_type IN ('CAR', 'BIKE', 'SUV', 'VAN')),
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    rental_rate NUMERIC(12, 2) NOT NULL CHECK (rental_rate >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    seating_capacity INT NULL,
    fuel_type VARCHAR(30) NULL,
    engine_capacity_cc INT NULL,
    four_wheel_drive BOOLEAN NULL,
    cargo_capacity_kg NUMERIC(10, 2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vehicles_type_active ON vehicles(vehicle_type, is_active);

-- 3. Ensure Bookings Table exists with interval constraints
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    vehicle_id VARCHAR(50) NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    pickup_date_time TIMESTAMP NOT NULL,
    return_date_time TIMESTAMP NOT NULL,
    rental_amount NUMERIC(12, 2) NOT NULL CHECK (rental_amount >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_dates CHECK (return_date_time > pickup_date_time)
);

CREATE INDEX IF NOT EXISTS idx_bookings_vehicle_dates_status
    ON bookings(vehicle_id, pickup_date_time, return_date_time, status);

CREATE INDEX IF NOT EXISTS idx_bookings_customer
    ON bookings(customer_id);

-- 4. Ensure Payments Table exists with method, status and transaction reference
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(50) PRIMARY KEY,
    booking_id VARCHAR(50) NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    payment_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING', 'CASH')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    transaction_reference VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);

-- 5. Ensure Maintenance Table exists for service records
CREATE TABLE IF NOT EXISTS maintenance (
    id VARCHAR(50) PRIMARY KEY,
    vehicle_id VARCHAR(50) NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    service_date DATE NOT NULL,
    description TEXT NOT NULL,
    cost NUMERIC(12, 2) NOT NULL CHECK (cost >= 0),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_maintenance_vehicle ON maintenance(vehicle_id);

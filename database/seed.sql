-- ==============================================================================
-- Ready2Ride: Vehicle Rental Management System
-- Development Seed Data Script (Idempotent)
-- Phase 2: PostgreSQL Database and JDBC Integration
-- ==============================================================================
-- NOTE: All data below is purely fictional and intended solely for development
-- and automated testing. Passwords are non-production development placeholders.
-- ==============================================================================

-- 1. Insert Initial Development Users (Customer & Administrator)
INSERT INTO users (id, full_name, email, phone_number, password_hash, role, driving_license_number, is_active)
VALUES
    ('CUST-DEV-001', 'Rahul Sharma', 'rahul.sharma@example.com', '+91-9876543210',
     '$2a$12$e8Y5tW7xGDevPlaceholderHashCustomerNotForProdUse001', 'CUSTOMER', 'DL-MH-2022-1234567', TRUE),
    ('CUST-DEV-002', 'Priya Patel', 'priya.patel@example.com', '+91-9876543211',
     '$2a$12$e8Y5tW7xGDevPlaceholderHashCustomerNotForProdUse002', 'CUSTOMER', 'DL-DL-2023-7654321', TRUE),
    ('ADMIN-DEV-001', 'System Administrator', 'admin@ready2ride.internal', '+91-9876543200',
     '$2a$12$e8Y5tW7xGDevPlaceholderHashAdminNotForProduction001', 'ADMIN', NULL, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Sample Fleet Vehicles Across All Subclasses (Car, Bike, SUV, Van)
-- A. Standard Car (Charged per started 24-hr period @ 2499.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, seating_capacity, fuel_type)
VALUES ('CAR-DEV-101', 'CAR', 'Honda', 'City', 'MH-12-DE-1001', 2499.00, TRUE, 5, 'Petrol')
ON CONFLICT (id) DO UPDATE SET rental_rate = EXCLUDED.rental_rate;

-- B. Two-Wheeler Bike (Charged per started 24-hr period @ 899.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, engine_capacity_cc)
VALUES ('BIKE-DEV-201', 'BIKE', 'Royal Enfield', 'Hunter 350', 'MH-12-DE-2002', 899.00, TRUE, 349)
ON CONFLICT (id) DO UPDATE SET rental_rate = EXCLUDED.rental_rate;

-- C. SUV (Charged per started 24-hr period @ 3500.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, seating_capacity, four_wheel_drive)
VALUES ('SUV-DEV-301', 'SUV', 'Mahindra', 'Scorpio-N', 'MH-12-DE-3003', 3500.00, TRUE, 7, TRUE)
ON CONFLICT (id) DO UPDATE SET rental_rate = EXCLUDED.rental_rate;

-- D. Van (Charged per started 24-hr period @ 4500.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, cargo_capacity_kg)
VALUES ('VAN-DEV-401', 'VAN', 'Force', 'Traveller', 'MH-12-DE-4004', 4500.00, TRUE, 1500.00)
ON CONFLICT (id) DO UPDATE SET rental_rate = EXCLUDED.rental_rate;

-- 3. (Sample bookings removed per Task 4: only real user-created reservations appear)
-- INSERT INTO bookings (id, customer_id, vehicle_id, pickup_date_time, return_date_time, rental_amount, status)...

-- 4. (Sample payment audit records removed per Task 4)
-- INSERT INTO payments (id, booking_id, amount, payment_date_time, payment_method, status, transaction_reference)...

-- 5. Insert Sample Maintenance Record
INSERT INTO maintenance (id, vehicle_id, service_date, description, cost, is_completed)
VALUES
    ('MNT-DEV-9001', 'SUV-DEV-301', '2026-10-01', 'Routine 10,000 km oil change and brake pad inspection', 120.00, TRUE)
ON CONFLICT (id) DO NOTHING;

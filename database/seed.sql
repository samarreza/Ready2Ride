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
-- A. Standard Car (Charged per started 24-hr period @ 55.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, seating_capacity, fuel_type)
VALUES ('CAR-DEV-101', 'CAR', 'Honda', 'City', 'MH-12-DE-1001', 55.00, TRUE, 5, 'Petrol')
ON CONFLICT (id) DO NOTHING;

-- B. Two-Wheeler Bike (Charged per started hour @ 15.00/hr)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, engine_capacity_cc)
VALUES ('BIKE-DEV-201', 'BIKE', 'Royal Enfield', 'Hunter 350', 'MH-12-DE-2002', 15.00, TRUE, 349)
ON CONFLICT (id) DO NOTHING;

-- C. SUV (Charged per started 24-hr period @ 85.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, seating_capacity, four_wheel_drive)
VALUES ('SUV-DEV-301', 'SUV', 'Mahindra', 'Scorpio-N', 'MH-12-DE-3003', 85.00, TRUE, 7, TRUE)
ON CONFLICT (id) DO NOTHING;

-- D. Van (Charged per started 24-hr period @ 110.00/day)
INSERT INTO vehicles (id, vehicle_type, brand, model, registration_number, rental_rate, is_active, cargo_capacity_kg)
VALUES ('VAN-DEV-401', 'VAN', 'Force', 'Traveller', 'MH-12-DE-4004', 110.00, TRUE, 1500.00)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert Sample Non-Overlapping Bookings for Testing
INSERT INTO bookings (id, customer_id, vehicle_id, pickup_date_time, return_date_time, rental_amount, status)
VALUES
    ('BKG-DEV-5001', 'CUST-DEV-001', 'CAR-DEV-101',
     '2026-11-01 10:00:00', '2026-11-03 10:00:00', 110.00, 'CONFIRMED'),
    ('BKG-DEV-5002', 'CUST-DEV-002', 'BIKE-DEV-201',
     '2026-11-01 14:00:00', '2026-11-01 17:00:00', 45.00, 'CONFIRMED')
ON CONFLICT (id) DO NOTHING;

-- 4. Insert Sample Payment Audit Record
INSERT INTO payments (id, booking_id, amount, payment_date_time, payment_method, status, transaction_reference)
VALUES
    ('PAY-DEV-7001', 'BKG-DEV-5001', 110.00, '2026-11-01 09:30:00', 'UPI', 'COMPLETED', 'UPI-REF-20261101-0001')
ON CONFLICT (id) DO NOTHING;

-- 5. Insert Sample Maintenance Record
INSERT INTO maintenance (id, vehicle_id, service_date, description, cost, is_completed)
VALUES
    ('MNT-DEV-9001', 'SUV-DEV-301', '2026-10-01', 'Routine 10,000 km oil change and brake pad inspection', 120.00, TRUE)
ON CONFLICT (id) DO NOTHING;

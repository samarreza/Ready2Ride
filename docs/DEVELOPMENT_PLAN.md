# Ready2Ride — Master Development Plan

**Project Name:** Ready2Ride — Vehicle Rental Management System  
**Architecture:** Multi-module Maven Application (`ready2ride-core`, `ready2ride-web`, `ready2ride-desktop`)

This document outlines the roadmap across all five engineering phases. Each phase builds incrementally on the foundation established by earlier phases.

---

## Phase Status Summary

| Phase | Title | Status | Primary Focus |
|---|---|---|---|
| **Phase 1** | **Core Java & OOP Foundation** | **COMPLETED** | OOP design, inheritance, polymorphism, custom exceptions, Collections, in-memory repos, unit tests |
| **Phase 2** | **PostgreSQL & JDBC Database Integration** | **COMPLETED (CURRENT)** | Relational schema, PostgreSQL driver, safe JDBC connection manager, `JdbcVehicleRepository`, `JdbcBookingRepository` with row locking (`FOR UPDATE`), integration tests |
| **Phase 3** | **Customer Web Application (Servlets & JSP)** | **PENDING** | HTTP request routing, Session management, JSP views, Web filters, WAR deployment |
| **Phase 4** | **Administrator Desktop Application (JavaFX)** | **PENDING** | JavaFX GUI, FXML views, Controller event handling, Fleet & reservation dashboard |
| **Phase 5** | **File Handling, Multithreading & Final Integration**| **PENDING** | Invoice/report file I/O, asynchronous processing, background tasks, end-to-end testing |

---

## Phase Details

### Phase 1: Core Java & OOP Foundation (Completed)
* **Goal:** Establish a robust domain model and business rules using core Java principles without external frameworks or database dependencies.
* **Scope & Deliverables:**
  - Multi-module Maven setup with `ready2ride-core`, `ready2ride-web`, and `ready2ride-desktop`.
  - Abstract `Vehicle` class with concrete subclasses: `Car`, `Bike`, `SUV`, and `Van`.
  - Polymorphic rental cost calculation (`calculateRentalCost`) enforcing started unit policies (hourly for bikes, 24h for cars/SUVs/vans).
  - Domain models: `Customer`, `Admin`, `Booking`, `Payment`, `MaintenanceRecord` with enums (`BookingStatus`, `PaymentStatus`, `PaymentMethod`, `VehicleType`).
  - Repository interfaces and in-memory implementations:
    - `InMemoryVehicleRepository` backed by `HashMap`.
    - `InMemoryBookingRepository` backed by `ArrayList`.
  - Service layer (`VehicleService`, `BookingService`) with date validation and conflict/overlap detection (`requestedPickup < existingReturn AND requestedReturn > existingPickup`).
  - User-defined exceptions: `InvalidRentalDateException`, `VehicleNotAvailableException`, `BookingNotFoundException`, `VehicleNotFoundException`.
  - Comprehensive unit test suite using JUnit 5.

---

### Phase 2: PostgreSQL, JDBC and Database Integration (Completed)
* **Goal:** Implement persistent relational storage using PostgreSQL and plain JDBC without external ORM frameworks.
* **Scope & Deliverables:**
  - Relational schema (`database/schema.sql`) for `users`, `vehicles`, `bookings`, `payments`, and `maintenance`.
  - Configurable, secure connection manager (`DatabaseConfig`, `DatabaseConnectionManager`) using environment variables (`READY2RIDE_DB_URL`, `READY2RIDE_DB_USER`, `READY2RIDE_DB_PASSWORD`) with masked secrets.
  - Plain JDBC repositories (`JdbcVehicleRepository`, `JdbcBookingRepository`) adhering to repository interfaces.
  - Concurrency control: Row-level locking (`SELECT ... FOR UPDATE`) on vehicle records during booking creation to eliminate race conditions between competing requests.
  - Transaction safety: Multi-step rollback support on constraint or overlap violations.
  - Development seed data (`database/seed.sql`) with idempotent insertion.
  - Comprehensive integration test suite (`PostgresIntegrationTest`) testing connection, CRUD, constraints, rollback, and concurrency.
  - Setup guide (`database/DATABASE_SETUP.md`) and `.gitignore` updates.
* **Deliberately Deferred:** Servlets, JSP pages, JavaFX GUI screens, FXML files, background multithreaded daemon jobs, invoice file generation.

---

### Phase 3: Customer Web Application with Servlets and JSP (Pending)
* **Goal:** Build a browser-based customer portal for browsing vehicles, booking rentals, and viewing booking status.
* **Scope & Deliverables:**
  - Packaging `ready2ride-web` as a complete WAR archive.
  - Servlet routing for HTTP GET/POST workflows (Browse Vehicles, Create Booking, View History).
  - JSP pages using JSTL and Expression Language (EL) for dynamic view rendering.
  - Session tracking for authenticated customers.
  - Request validation filters and error page dispatchers.

---

### Phase 4: JavaFX Administrator Desktop Application (Pending)
* **Goal:** Build an administrative desktop GUI for fleet managers to manage vehicles, approve bookings, and inspect maintenance records.
* **Scope & Deliverables:**
  - JavaFX dependency integration in `ready2ride-desktop`.
  - FXML view layouts for Fleet Overview, Booking Management, and Vehicle Status.
  - JavaFX Controllers with observable collections (`ObservableList`) bound to tables and charts.
  - Modal dialogs for adding/editing vehicles and managing reservations.

---

### Phase 5: File Handling, Multithreading, Integration and Final Testing (Pending)
* **Goal:** Implement advanced Java features for background jobs, reporting, file export/import, and comprehensive system testing.
* **Scope & Deliverables:**
  - Java File I/O: Exporting invoices and rental agreements (PDF or text/CSV formats) and batch vehicle import.
  - Java Multithreading: Background threads for checking overdue returns, sending reminder notifications, and generating periodic reports.
  - Thread safety and concurrency controls.
  - Cross-module integration tests and user acceptance workflows.

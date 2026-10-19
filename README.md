# Ready2Ride — Vehicle Rental Management System

**Ready2Ride** is a vehicle rental management application designed to handle fleet administration and customer rental reservations across bikes, cars, SUVs, and vans.

This repository implements **Phase 1: Project Foundation and Core Java** and **Phase 2: PostgreSQL Database and JDBC Integration**, establishing core domain modeling, object-oriented principles, custom exceptions, Collections, PostgreSQL relational schema, and plain JDBC persistence with transaction-safe concurrency control.

---

## Maven Multi-Module Architecture

The project is structured as a Maven multi-module system to cleanly separate business logic and persistence from future user interfaces:

```text
java project/
│
├── pom.xml                                   # Parent POM managing dependencies and plugins
├── README.md                                 # Project overview and instructions
├── .gitignore                                # Prevents committing build outputs and secrets
├── mvnw / mvnw.cmd / .mvn/                   # Maven Wrapper portable build scripts
│
├── database/                                 # Database scripts and guides (Phase 2)
│   ├── schema.sql                            # DDL table creation and constraints
│   ├── seed.sql                              # Idempotent development seed data
│   └── DATABASE_SETUP.md                     # Comprehensive database setup guide
│
├── ready2ride-core/                          # Shared domain models, repositories, and services
│   ├── pom.xml                               # Core JAR POM (PostgreSQL JDBC driver included)
│   └── src/
│       ├── main/java/com/ready2ride/core/
│       │   ├── model/                        # Vehicles, Customer, Booking, Payment, Enums
│       │   ├── service/                      # VehicleService, BookingService
│       │   ├── repository/                   # Repository interfaces, in-memory & factory
│       │   │   └── jdbc/                     # JdbcVehicleRepository, JdbcBookingRepository
│       │   ├── database/                     # DatabaseConfig, DatabaseConnectionManager
│       │   ├── exception/                    # Domain & persistence custom exceptions
│       │   └── util/                         # Date and interval utility methods
│       └── test/java/com/ready2ride/core/    # JUnit 5 unit & integration test suites
│
├── ready2ride-web/                           # Future customer web app (Servlets & JSP)
│   └── pom.xml                               # WAR scaffold (Phase 3)
│
├── ready2ride-desktop/                       # Future admin desktop app (JavaFX)
│   └── pom.xml                               # JAR scaffold (Phase 4)
│
└── docs/
    └── DEVELOPMENT_PLAN.md                   # 5-phase architectural roadmap
```

---

## Implemented Architecture

### 1. Phase 1: Core Java Foundation
* **Inheritance & Abstract Classes:** Abstract `Vehicle` defines shared fleet attributes and abstract `calculateRentalCost(pickup, return)`. Subclasses `Car`, `Bike`, `SUV`, and `Van` provide concrete pricing policies.
* **Polymorphism:** Subtypes are stored and processed via `Vehicle` references. `BookingService` calculates rental charges polymorphically.
* **Collections & In-Memory Repositories:** `InMemoryVehicleRepository` (`HashMap`) and `InMemoryBookingRepository` (`ArrayList`) provide decoupled in-memory persistence.
* **Custom Exceptions:** `InvalidRentalDateException`, `VehicleNotAvailableException`, `BookingNotFoundException`, `VehicleNotFoundException`.

### 2. Phase 2: PostgreSQL & Plain JDBC Persistence
* **PostgreSQL Schema (`database/schema.sql`):**
  - `users`: Account management with unique emails, password hashes, and role checks (`CUSTOMER`, `ADMIN`).
  - `vehicles`: Polymorphic vehicle records with subtype-specific attributes and fixed-precision `NUMERIC(12, 2)` rental rates.
  - `bookings`: Foreign-keyed reservation records enforcing chronological check constraint `return_date_time > pickup_date_time`.
  - `payments` & `maintenance`: Audit tables for transaction and fleet upkeep records.
  - **Timezone Policy:** All timestamps use `TIMESTAMP WITHOUT TIME ZONE` representing local business time, mapped to `LocalDateTime`.
* **Safe JDBC Persistence:**
  - `DatabaseConfig`: Loads connection details from `READY2RIDE_DB_URL`, `READY2RIDE_DB_USER`, and `READY2RIDE_DB_PASSWORD`. Masks passwords in string representations.
  - `DatabaseConnectionManager`: Provides on-demand connections using try-with-resources. Never leaks credentials.
  - `JdbcVehicleRepository`: Manages CRUD and polymorphic reconstruction across `Car`, `Bike`, `SUV`, and `Van`.
  - `JdbcBookingRepository`: Enforces database transactions with row-level pessimistic locking (`SELECT ... FOR UPDATE`) to eliminate double-booking race conditions during simultaneous reservation requests.
  - `RepositoryFactory`: Decoupled factory providing either in-memory or JDBC repositories.

---

## Compiling and Testing

### Prerequisites
* **Java:** JDK 17 or higher (tested with JDK 26).
* **Maven:** Apache Maven 3.9+ or use the included `./mvnw.cmd`.
* **PostgreSQL:** Version 14+ (for live integration tests).

### Automated Testing

1. **Run Unit Tests (Runs offline without database):**
   ```powershell
   .\mvnw.cmd clean test
   ```

2. **Run PostgreSQL Integration Tests (Requires configured database):**
   ```powershell
   $env:READY2RIDE_DB_URL = "jdbc:postgresql://localhost:5432/ready2ride"
   $env:READY2RIDE_DB_USER = "postgres"
   $env:READY2RIDE_DB_PASSWORD = "your_password"
   .\mvnw.cmd test -Dtest=PostgresIntegrationTest
   ```
   *(If the database is not accessible or credentials are not supplied, integration tests are cleanly skipped with an informative diagnostic message rather than failing the build).*

---

## Strictly Deferred Items (Not in Phase 2)

In adherence to Phase 2 boundaries:
* **No Web UI:** No Servlets, JSP files, HTML, CSS, or JavaScript (Reserved for Phase 3).
* **No Desktop GUI:** No JavaFX components, FXML layouts, or controllers (Reserved for Phase 4).
* **No File I/O or Multithreading Jobs:** Invoice generation and background threads belong to Phase 5.
* **No Third-Party Frameworks:** Pure JDBC and standard Java without Spring Boot, Hibernate, or JPA.

# Ready2Ride — PostgreSQL Database Setup Guide

This guide details the setup and configuration process for connecting the **Ready2Ride** Java application to a local or remote PostgreSQL database.

---

## 1. Prerequisites

* **PostgreSQL:** Version 14 or higher (e.g., PostgreSQL 18).
* **Verify PostgreSQL is Running:**
  - On Windows, check the service status in PowerShell:
    ```powershell
    Get-Service postgresql*
    ```
  - Ensure port `5432` is listening:
    ```powershell
    Get-NetTCPConnection -LocalPort 5432 -State Listen
    ```

---

## 2. Database Creation

Open your terminal or `psql` command-line client:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres
```

Inside the PostgreSQL prompt, create the dedicated project database:

```sql
CREATE DATABASE ready2ride;
```

To verify the database was created:

```sql
\l
```

Exit `psql`:

```sql
\q
```

---

## 3. Environment Variables Configuration

Ready2Ride avoids hard-coded database credentials in source code. Configure the following environment variables:

| Variable | Description | Default / Example |
|---|---|---|
| `READY2RIDE_DB_URL` | JDBC Connection URL | `jdbc:postgresql://localhost:5432/ready2ride` |
| `READY2RIDE_DB_USER` | PostgreSQL Username | `postgres` |
| `READY2RIDE_DB_PASSWORD` | PostgreSQL User Password | *Your local database password* |

### Setting Environment Variables in PowerShell (Current Session)

```powershell
$env:READY2RIDE_DB_URL = "jdbc:postgresql://localhost:5432/ready2ride"
$env:READY2RIDE_DB_USER = "postgres"
$env:READY2RIDE_DB_PASSWORD = "YOUR_DATABASE_PASSWORD"
```

### Setting Environment Variables Permanently (User Level)

```powershell
[Environment]::SetEnvironmentVariable("READY2RIDE_DB_URL", "jdbc:postgresql://localhost:5432/ready2ride", "User")
[Environment]::SetEnvironmentVariable("READY2RIDE_DB_USER", "postgres", "User")
[Environment]::SetEnvironmentVariable("READY2RIDE_DB_PASSWORD", "YOUR_DATABASE_PASSWORD", "User")
```

> **Security Notice:** Never commit actual passwords or `.env` files to source control. The project's `.gitignore` automatically blocks credentials files.

---

## 4. Applying Schema and Seed Data

### Applying the Schema DDL

Run the schema script against the `ready2ride` database:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d ready2ride -f "database\schema.sql"
```

This creates the following tables:
* `users` — Customers and administrators with uniqueness and role constraints.
* `vehicles` — Polymorphic fleet catalog (Car, Bike, SUV, Van) with subtype attributes and fixed-precision `rental_rate NUMERIC(12, 2)`.
* `bookings` — Reservations with foreign keys and check constraint `return_date_time > pickup_date_time`.
* `payments` — Transaction audit records.
* `maintenance` — Fleet service and repair logs.

### Applying Optional Development Seed Data

To populate sample test records (vehicles, demo customer, and non-overlapping reservations):

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d ready2ride -f "database\seed.sql"
```

The seed script is idempotent (`ON CONFLICT DO NOTHING`) and safe to re-run.

---

## 5. Provisioning the Initial Administrator (Bootstrap Setup)

Ready2Ride avoids hard-coded administrator credentials in source code. To safely initialize the first system administrator:

```powershell
.\mvnw.cmd exec:java -pl ready2ride-core -Dexec.mainClass="com.ready2ride.core.database.AdminBootstrapCli"
```

The bootstrap CLI:
1. Verifies connectivity to the `ready2ride` PostgreSQL database.
2. Checks whether an administrator account already exists.
3. Prompts securely for Administrator Full Name, Email, Phone Number, and Password.
4. Hashes the password using enterprise PBKDF2 with HMAC-SHA256 (65,536 iterations, 128-bit salt).
5. Persists the administrator account with the `ADMIN` role.
6. **Safety Guarantee:** If an administrator account already exists, the bootstrap process aborts immediately and refuses to overwrite.

---

## 6. Running the JavaFX Desktop Application

To launch the administrator desktop application:

```powershell
.\mvnw.cmd javafx:run -pl ready2ride-desktop
```

Ensure environment variables (`READY2RIDE_DB_URL`, `READY2RIDE_DB_USER`, `READY2RIDE_DB_PASSWORD`) are configured in the terminal session before running so that login authentication can connect to PostgreSQL. If PostgreSQL is not reachable, the login screen displays a clear, safe diagnostic error message.

---

## 7. Running Automated Tests

### Category A: Unit Tests (No Database Required)

Existing Phase 1 unit tests run completely offline:

```powershell
.\mvnw.cmd test -Dtest="!PostgresIntegrationTest"
```

### Category B: Full Test Suite with PostgreSQL Integration Tests

Once environment variables are set and the database is reachable:

```powershell
.\mvnw.cmd clean test
```

Or run the PostgreSQL integration suite directly:

```powershell
.\mvnw.cmd test -Dtest=PostgresIntegrationTest
```

> If `READY2RIDE_DB_USER` and `READY2RIDE_DB_PASSWORD` are not configured, `PostgresIntegrationTest` will gracefully skip without failing the build.

---

## 8. Common Connection Troubleshooting

1. **Error: `Connection refused: connect`**
   - **Cause:** PostgreSQL service is stopped or running on a different port.
   - **Fix:** Start the service using `Start-Service postgresql-x64-18` (as Administrator).

2. **Error: `password authentication failed for user`**
   - **Cause:** Incorrect value in `READY2RIDE_DB_PASSWORD` or `READY2RIDE_DB_USER`.
   - **Fix:** Update `$env:READY2RIDE_DB_PASSWORD` with the password chosen during PostgreSQL installation.

3. **Error: `database "ready2ride" does not exist`**
   - **Cause:** Database creation command was not run.
   - **Fix:** Run `CREATE DATABASE ready2ride;` via `psql`.

4. **Error: `column "..." does not exist`**
   - **Cause:** The schema has not been initialized.
   - **Fix:** Execute `database\schema.sql`.

package com.ready2ride.core.database;

import com.ready2ride.core.model.User;
import com.ready2ride.core.repository.RepositoryFactory;
import com.ready2ride.core.repository.UserRepository;
import com.ready2ride.core.service.AuthenticationService;

import java.io.Console;
import java.util.Scanner;

/**
 * Controlled command-line bootstrap utility for initializing the first Ready2Ride administrator.
 * Prompts interactively for administrator credentials, hashes the password securely using PBKDF2,
 * and strictly refuses to overwrite existing administrators.
 */
public class AdminBootstrapCli {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Ready2Ride — Administrator Bootstrap Setup");
        System.out.println("==================================================");

        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager();
        if (!connectionManager.isDatabaseAvailable(3)) {
            System.err.println("Error: PostgreSQL database is not reachable.");
            System.err.println("Ensure PostgreSQL is running and environment variables (READY2RIDE_DB_URL, READY2RIDE_DB_USER, READY2RIDE_DB_PASSWORD) are set.");
            System.exit(1);
        }

        UserRepository userRepository = RepositoryFactory.createJdbcUserRepository(connectionManager);
        AuthenticationService authService = new AuthenticationService(userRepository);

        if (authService.hasAdministrator()) {
            System.out.println("\n[NOTICE] An administrator account already exists in this database.");
            System.out.println("Bootstrap aborted: System refuses to overwrite existing administrator accounts.");
            return;
        }

        Console console = System.console();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("\nEnter Administrator Full Name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Enter Administrator Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Administrator Phone Number: ");
            String phone = scanner.nextLine().trim();

            String password;
            if (console != null) {
                char[] passChars = console.readPassword("Enter Administrator Password (hidden): ");
                password = new String(passChars);
            } else {
                System.out.print("Enter Administrator Password: ");
                password = scanner.nextLine();
            }

            if (password.length() < 6) {
                System.err.println("\nError: Password must be at least 6 characters.");
                System.exit(1);
            }

            User admin = authService.bootstrapInitialAdmin(fullName, email, phone, password);
            System.out.println("\n[SUCCESS] Administrator successfully provisioned!");
            System.out.println("  Admin ID:   " + admin.getId());
            System.out.println("  Full Name:  " + admin.getFullName());
            System.out.println("  Email:      " + admin.getEmail());
            System.out.println("  Role:       " + admin.getRole());
            System.out.println("You may now log in to the Ready2Ride desktop application.");

        } catch (Exception e) {
            System.err.println("\n[FAILURE] Bootstrap failed: " + e.getMessage());
            System.exit(1);
        }
    }
}

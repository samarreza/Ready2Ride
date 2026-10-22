package com.ready2ride.desktop;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.database.DemoDataInitializer;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.VehicleType;
import com.ready2ride.core.repository.BookingRepository;
import com.ready2ride.core.repository.FavoritesRepository;
import com.ready2ride.core.repository.MaintenanceRepository;
import com.ready2ride.core.repository.PaymentRepository;
import com.ready2ride.core.repository.RepositoryFactory;
import com.ready2ride.core.repository.UserRepository;
import com.ready2ride.core.repository.VehicleImageRepository;
import com.ready2ride.core.repository.VehicleRepository;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.FavoritesService;
import com.ready2ride.core.service.MaintenanceService;
import com.ready2ride.core.service.PaymentService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.controller.DashboardController;
import com.ready2ride.desktop.controller.HomeController;
import com.ready2ride.desktop.controller.LoginController;
import com.ready2ride.desktop.view.CustomerDashboardView;
import com.ready2ride.desktop.view.DashboardView;
import com.ready2ride.desktop.view.HomeView;
import com.ready2ride.desktop.view.LoginView;
import com.ready2ride.desktop.view.RegistrationView;
import com.ready2ride.desktop.view.VehicleCatalogView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaFX application entry point for Ready2Ride.
 * Manages Stage lifecycle, PostgreSQL services wiring, demo fleet & users seeding,
 * and comprehensive navigation across:
 * - Home Landing Page
 * - Vehicle Catalog (matching Screenshot 1)
 * - Vehicle Details Modal (matching Screenshot 4)
 * - Payment & Confirmation Modal (matching Screenshot 5)
 * - Two-Column Login View (matching Screenshot 3)
 * - Customer Registration View
 * - Customer Dashboard & My Bookings (Section 9)
 * - Administrator Console (Sections 10 to 16)
 */
public class Ready2RideApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(Ready2RideApplication.class.getName());

    public static final String APP_TITLE = "Ready2Ride — Vehicle Rental System";
    public static final String CATALOG_TITLE = "Ready2Ride — Explore Our Vehicles";
    public static final String LOGIN_TITLE = "Ready2Ride — Sign In";
    public static final String REGISTER_TITLE = "Ready2Ride — Create an Account";
    public static final String CUSTOMER_DASHBOARD_TITLE = "Ready2Ride — Customer Dashboard & My Bookings";
    public static final String DASHBOARD_TITLE = "Ready2Ride — Rental Provider Dashboard";

    private Stage primaryStage;
    private DatabaseConnectionManager connectionManager;
    private UserRepository userRepository;
    private VehicleRepository vehicleRepository;
    private VehicleImageRepository vehicleImageRepository;
    private BookingRepository bookingRepository;
    private PaymentRepository paymentRepository;
    private MaintenanceRepository maintenanceRepository;
    private FavoritesRepository favoritesRepository;

    private AuthenticationService authService;
    private VehicleService vehicleService;
    private BookingService bookingService;
    private PaymentService paymentService;
    private MaintenanceService maintenanceService;
    private FavoritesService favoritesService;

    @Override
    public void init() {
        LOGGER.info("Initializing Ready2Ride desktop application backend services...");
        try {
            connectionManager = new DatabaseConnectionManager();

            // Non-destructive schema migration for Rental Provider role and vehicle ownership
            try (java.sql.Connection conn = connectionManager.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;");
                stmt.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('CUSTOMER', 'RENTAL_PROVIDER', 'ADMIN'));");
                stmt.execute("ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS provider_id VARCHAR(50) NULL REFERENCES users(id) ON DELETE SET NULL;");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_vehicles_provider ON vehicles(provider_id);");
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Notice during schema verification: " + e.getMessage());
            }

            userRepository = RepositoryFactory.createJdbcUserRepository(connectionManager);
            vehicleRepository = RepositoryFactory.createJdbcVehicleRepository(connectionManager);
            vehicleImageRepository = RepositoryFactory.createJdbcVehicleImageRepository(connectionManager);
            bookingRepository = RepositoryFactory.createJdbcBookingRepository(connectionManager);
            paymentRepository = RepositoryFactory.createJdbcPaymentRepository(connectionManager);
            maintenanceRepository = RepositoryFactory.createJdbcMaintenanceRepository(connectionManager);
            favoritesRepository = RepositoryFactory.createJdbcFavoritesRepository(connectionManager);

            authService = new AuthenticationService(userRepository);
            vehicleService = new VehicleService(vehicleRepository, vehicleImageRepository);
            bookingService = new BookingService(bookingRepository, vehicleRepository, maintenanceRepository);
            paymentService = new PaymentService(paymentRepository, bookingRepository);
            maintenanceService = new MaintenanceService(maintenanceRepository, vehicleRepository);
            favoritesService = new FavoritesService(favoritesRepository, vehicleRepository);

            // Safe, idempotent seeding of demonstration vehicles, images, and demo accounts into PostgreSQL
            DemoDataInitializer demoInitializer = new DemoDataInitializer(vehicleRepository, vehicleImageRepository);
            demoInitializer.seedDemoFleetIfAbsent();
            demoInitializer.seedExtendedFleetIfAbsent();
            demoInitializer.seedDemoUsersIfAbsent(userRepository);

            LOGGER.info("Backend services and PostgreSQL demo fleet verified successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Warning during backend initialization / demo fleet seeding: " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(1120);
        primaryStage.setMinHeight(740);

        showHomeScene();
        primaryStage.show();
    }

    /**
     * Constructs and displays the modernized Customer Home Page scene.
     */
    public void showHomeScene() {
        primaryStage.setTitle(APP_TITLE);

        HomeView homeView = new HomeView();
        new HomeController(
                homeView,
                vehicleService,
                bookingService,
                authService,
                this::showVehicleCatalogScene,
                this::showVehicleCatalogScene,
                this::showLoginScene,
                this::showRegisterScene
        );

        Scene homeScene = new Scene(homeView.getRootNode(), 1380, 880);
        primaryStage.setScene(homeScene);
    }

    /**
     * Constructs and displays the Vehicle Catalog scene.
     */
    public void showVehicleCatalogScene(VehicleType initialFilter) {
        showVehicleCatalogScene(initialFilter, null, null, null);
    }

    /**
     * Constructs and displays the Vehicle Catalog scene with search criteria.
     */
    public void showVehicleCatalogScene(VehicleType initialFilter, LocalDate pickup, LocalDate returnDate, String location) {
        primaryStage.setTitle(CATALOG_TITLE);

        VehicleCatalogView catalogView = new VehicleCatalogView(
                vehicleService,
                bookingService,
                paymentService,
                favoritesService,
                this::showHomeScene,
                this::showLoginScene,
                this::showRegisterScene,
                () -> showCustomerDashboardScene(null),
                initialFilter,
                pickup,
                returnDate,
                location
        );

        Scene catalogScene = new Scene(catalogView.getRootNode(), 1380, 880);
        primaryStage.setScene(catalogScene);
    }

    /**
     * Constructs and displays the two-column Login scene matching Screenshot 3.
     */
    public void showLoginScene() {
        primaryStage.setTitle(LOGIN_TITLE);

        LoginView loginView = new LoginView();
        new LoginController(
                loginView,
                authService,
                this::showDashboardScene,
                this::showHomeScene,
                this::showRegisterScene,
                this::showCustomerDashboardScene
        );

        Scene loginScene = new Scene(loginView.getRootNode(), 1380, 880);
        primaryStage.setScene(loginScene);
    }

    /**
     * Constructs and displays the Customer Registration scene.
     */
    public void showRegisterScene() {
        primaryStage.setTitle(REGISTER_TITLE);

        RegistrationView regView = new RegistrationView(
                authService,
                this::showLoginScene,
                this::showHomeScene
        );

        Scene regScene = new Scene(regView.getRootNode(), 1380, 880);
        primaryStage.setScene(regScene);
    }

    /**
     * Constructs and displays the Customer Dashboard & My Bookings scene.
     */
    public void showCustomerDashboardScene(User user) {
        primaryStage.setTitle(CUSTOMER_DASHBOARD_TITLE);

        CustomerDashboardView customerDashboard = new CustomerDashboardView(
                bookingService,
                vehicleService,
                paymentService,
                favoritesService,
                authService,
                this::showHomeScene,
                () -> showVehicleCatalogScene(null)
        );

        Scene custScene = new Scene(customerDashboard.getRootNode(), 1380, 880);
        primaryStage.setScene(custScene);
    }

    /**
     * Constructs and displays the Administrator Console scene.
     */
    public void showDashboardScene(String username) {
        primaryStage.setTitle(DASHBOARD_TITLE);

        DashboardView dashboardView = new DashboardView();
        dashboardView.setAdminGreeting(username);

        new DashboardController(
                dashboardView,
                vehicleService,
                bookingService,
                authService,
                paymentService,
                maintenanceService,
                this::showHomeScene
        );

        Scene dashboardScene = new Scene(dashboardView.getRootNode(), 1380, 880);
        primaryStage.setScene(dashboardScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

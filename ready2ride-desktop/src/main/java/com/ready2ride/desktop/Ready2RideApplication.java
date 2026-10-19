package com.ready2ride.desktop;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.repository.RepositoryFactory;
import com.ready2ride.core.repository.UserRepository;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.desktop.controller.DashboardController;
import com.ready2ride.desktop.controller.LoginController;
import com.ready2ride.desktop.view.DashboardView;
import com.ready2ride.desktop.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX application entry point for Ready2Ride.
 * Manages Stage lifecycle, PostgreSQL authentication service wiring,
 * and view navigation between Login and Administrator Dashboard.
 */
public class Ready2RideApplication extends Application {

    public static final String APP_TITLE = "Ready2Ride — Admin Login";
    public static final String DASHBOARD_TITLE = "Ready2Ride — Administrator Dashboard";

    private Stage primaryStage;
    private AuthenticationService authService;

    @Override
    public void init() {
        // Initialize persistent database connection and authentication service
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager();
        UserRepository userRepository = RepositoryFactory.createJdbcUserRepository(connectionManager);
        this.authService = new AuthenticationService(userRepository);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(600);

        showLoginScene();
        primaryStage.show();
    }

    /**
     * Constructs and displays the Login scene.
     */
    public void showLoginScene() {
        primaryStage.setTitle(APP_TITLE);

        LoginView loginView = new LoginView();
        new LoginController(loginView, authService, this::showDashboardScene);

        Scene loginScene = new Scene(loginView.getRootNode(), 540, 720);
        primaryStage.setScene(loginScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Constructs and displays the Administrator Dashboard scene.
     *
     * @param username user identifier displayed in header greeting
     */
    public void showDashboardScene(String username) {
        primaryStage.setTitle(DASHBOARD_TITLE);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);

        DashboardView dashboardView = new DashboardView();
        dashboardView.setAdminGreeting(username);

        new DashboardController(dashboardView, this::showLoginScene);

        Scene dashboardScene = new Scene(dashboardView.getRootNode(), 1140, 750);
        primaryStage.setScene(dashboardScene);
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.ready2ride.desktop.controller;

import com.ready2ride.core.exception.AuthenticationException;
import com.ready2ride.core.exception.DatabaseConnectionException;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.desktop.view.LoginView;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Controller managing user interactions on the Ready2Ride Login screen.
 * Connects directly to {@link AuthenticationService} for persistent database authentication,
 * enforces role authorizations, and handles database connectivity errors gracefully.
 */
public class LoginController {

    private final LoginView view;
    private final AuthenticationService authService;
    private final Consumer<String> onNavigateToDashboard;
    private boolean isPasswordVisible = false;

    /**
     * Constructs the LoginController with persistent authentication service.
     *
     * @param view                   LoginView presentation instance
     * @param authService            backend authentication service
     * @param onNavigateToDashboard callback consumer triggered when navigating to dashboard
     */
    public LoginController(LoginView view, AuthenticationService authService, Consumer<String> onNavigateToDashboard) {
        this.view = Objects.requireNonNull(view, "LoginView must not be null");
        this.authService = authService;
        this.onNavigateToDashboard = Objects.requireNonNull(onNavigateToDashboard, "Navigation callback must not be null");

        initEventHandlers();
    }

    /**
     * Backwards-compatible constructor for testing.
     */
    public LoginController(LoginView view, Consumer<String> onNavigateToDashboard) {
        this(view, null, onNavigateToDashboard);
    }

    private void initEventHandlers() {
        // Sign In Action
        view.getSignInButton().setOnAction(e -> handleSignIn());

        // Preview Dashboard Action
        view.getPreviewDashboardButton().setOnAction(e -> handlePreviewDashboard());

        // Password Show/Hide Toggle
        view.getTogglePasswordBtn().setOnAction(e -> togglePasswordVisibility());

        // Synchronize text between masked PasswordField and unmasked TextField
        view.getPasswordField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isPasswordVisible) {
                view.getUnmaskedPasswordField().setText(newVal);
            }
        });
        view.getUnmaskedPasswordField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (isPasswordVisible) {
                view.getPasswordField().setText(newVal);
            }
        });

        // Trigger Sign In on Enter key
        view.getEmailField().setOnAction(e -> handleSignIn());
        view.getPasswordField().setOnAction(e -> handleSignIn());
        view.getUnmaskedPasswordField().setOnAction(e -> handleSignIn());
    }

    private void handleSignIn() {
        view.clearError();

        String username = view.getEmailField().getText() != null ? view.getEmailField().getText().trim() : "";
        String password = isPasswordVisible
                ? (view.getUnmaskedPasswordField().getText() != null ? view.getUnmaskedPasswordField().getText() : "")
                : (view.getPasswordField().getText() != null ? view.getPasswordField().getText() : "");

        // 1. Validation for missing fields
        if (username.isEmpty() || password.isEmpty()) {
            view.showError("Please enter both your email/username and password.");
            return;
        }

        if (authService == null) {
            view.showError("Authentication service is uninitialized. Please check database configuration.");
            return;
        }

        try {
            // 2. Perform database-backed authentication
            User user = authService.authenticate(username, password);

            // 3. Verify the account's role (enforce ADMIN access for desktop)
            if (user.getRole() != UserRole.ADMIN) {
                view.showError("Access denied: Only administrator accounts can access the Fleet Management Dashboard.");
                return;
            }

            // 4. Enter administrator dashboard upon successful verification
            onNavigateToDashboard.accept(user.getFullName());

        } catch (AuthenticationException e) {
            view.showError(e.getMessage());
        } catch (DatabaseConnectionException e) {
            view.showError("Unable to connect to PostgreSQL database. Please ensure PostgreSQL is running and credentials are configured.");
        } catch (Exception e) {
            view.showError("Sign in error: " + e.getMessage());
        }
    }

    private void handlePreviewDashboard() {
        view.clearError();
        // Direct demonstration navigation with default preview user (does not count as authenticated login)
        onNavigateToDashboard.accept("Admin Preview (Demo)");
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            view.getUnmaskedPasswordField().setText(view.getPasswordField().getText());
            view.getPasswordField().setVisible(false);
            view.getPasswordField().setManaged(false);
            view.getUnmaskedPasswordField().setVisible(true);
            view.getUnmaskedPasswordField().setManaged(true);
            view.getTogglePasswordBtn().setText("Hide");
            view.getUnmaskedPasswordField().requestFocus();
            view.getUnmaskedPasswordField().positionCaret(view.getUnmaskedPasswordField().getText().length());
        } else {
            view.getPasswordField().setText(view.getUnmaskedPasswordField().getText());
            view.getUnmaskedPasswordField().setVisible(false);
            view.getUnmaskedPasswordField().setManaged(false);
            view.getPasswordField().setVisible(true);
            view.getPasswordField().setManaged(true);
            view.getTogglePasswordBtn().setText("Show");
            view.getPasswordField().requestFocus();
            view.getPasswordField().positionCaret(view.getPasswordField().getText().length());
        }
    }
}

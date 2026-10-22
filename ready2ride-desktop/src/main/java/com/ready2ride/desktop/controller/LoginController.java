package com.ready2ride.desktop.controller;

import com.ready2ride.core.exception.AuthenticationException;
import com.ready2ride.core.exception.DatabaseConnectionException;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.desktop.util.UserSession;
import com.ready2ride.desktop.view.LoginView;
import javafx.scene.control.Alert;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Controller managing user authentication and navigation on the Ready2Ride Login screen.
 * Connects directly to {@link AuthenticationService} for persistent database authentication,
 * routes administrators to Fleet Management, and routes customers to their Dashboard/Bookings.
 */
public class LoginController {

    private final LoginView view;
    private final AuthenticationService authService;
    private final Consumer<String> onNavigateToDashboard;
    private final Runnable onNavigateBackToHome;
    private final Runnable onNavigateToRegister;
    private final Consumer<User> onCustomerLoginSuccess;
    private boolean isPasswordVisible = false;

    public LoginController(LoginView view, AuthenticationService authService,
                           Consumer<String> onNavigateToDashboard,
                           Runnable onNavigateBackToHome,
                           Runnable onNavigateToRegister,
                           Consumer<User> onCustomerLoginSuccess) {
        this.view = Objects.requireNonNull(view, "LoginView must not be null");
        this.authService = authService;
        this.onNavigateToDashboard = Objects.requireNonNull(onNavigateToDashboard, "Dashboard navigation callback must not be null");
        this.onNavigateBackToHome = onNavigateBackToHome;
        this.onNavigateToRegister = onNavigateToRegister;
        this.onCustomerLoginSuccess = onCustomerLoginSuccess;

        initEventHandlers();
    }

    public LoginController(LoginView view, AuthenticationService authService,
                           Consumer<String> onNavigateToDashboard, Runnable onNavigateBackToHome) {
        this(view, authService, onNavigateToDashboard, onNavigateBackToHome, null, null);
    }

    public LoginController(LoginView view, AuthenticationService authService, Consumer<String> onNavigateToDashboard) {
        this(view, authService, onNavigateToDashboard, null, null, null);
    }

    public LoginController(LoginView view, Consumer<String> onNavigateToDashboard) {
        this(view, null, onNavigateToDashboard, null, null, null);
    }

    private void initEventHandlers() {
        if (view.getBackToHomeButton() != null) {
            view.getBackToHomeButton().setOnAction(e -> {
                if (onNavigateBackToHome != null) {
                    onNavigateBackToHome.run();
                }
            });
        }

        // Sign In Action
        view.getSignInButton().setOnAction(e -> handleSignIn());

        // Create Account Action Card
        if (view.getCreateAccountCard() != null) {
            view.getCreateAccountCard().setOnMouseClicked(e -> {
                if (onNavigateToRegister != null) {
                    onNavigateToRegister.run();
                }
            });
        }

        // Preview Dashboard Action Card
        if (view.getPreviewDashboardCard() != null) {
            view.getPreviewDashboardCard().setOnMouseClicked(e -> handlePreviewDashboard());
        }

        // Forgot Password link
        if (view.getForgotPasswordLink() != null) {
            view.getForgotPasswordLink().setOnMouseClicked(e -> handleForgotPassword());
        }

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

        if (username.isEmpty() || password.isEmpty()) {
            view.showError("Please enter both your email/username and password.");
            return;
        }

        if (authService == null) {
            view.showError("Authentication service is uninitialized. Please verify database configuration.");
            return;
        }

        try {
            // Perform database-backed authentication
            User user = authService.authenticate(username, password);
            UserSession.getInstance().login(user);

            if (user.getRole() == UserRole.ADMIN) {
                onNavigateToDashboard.accept(user.getFullName());
            } else {
                if (onCustomerLoginSuccess != null) {
                    onCustomerLoginSuccess.accept(user);
                } else if (onNavigateBackToHome != null) {
                    onNavigateBackToHome.run();
                }
            }
        } catch (AuthenticationException e) {
            view.showError(e.getMessage());
        } catch (DatabaseConnectionException e) {
            view.showError("Unable to connect to PostgreSQL database. Please verify your connection.");
        } catch (Exception e) {
            view.showError("Sign in error: " + e.getMessage());
        }
    }

    private void handlePreviewDashboard() {
        view.clearError();
        UserSession.getInstance().loginAsDemo();
        onNavigateToDashboard.accept("Admin Preview (Demo)");
    }

    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Reset Your Ready2Ride Password");
        alert.setContentText("""
                For customer and fleet security, password resets are processed through verified contact details.
                
                Please contact support at support@ready2ride.com or your fleet administrator with your registered email and driving license number to receive a temporary reset token.
                
                Default demo accounts:
                • Admin: admin@ready2ride.com / Admin@123
                • Customer: john.doe@ready2ride.com / Customer@123
                """);
        alert.showAndWait();
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            view.getUnmaskedPasswordField().setText(view.getPasswordField().getText());
            view.getPasswordField().setVisible(false);
            view.getPasswordField().setManaged(false);
            view.getUnmaskedPasswordField().setVisible(true);
            view.getUnmaskedPasswordField().setManaged(true);
            view.getTogglePasswordBtn().setText("🙈");
            view.getUnmaskedPasswordField().requestFocus();
            view.getUnmaskedPasswordField().positionCaret(view.getUnmaskedPasswordField().getText().length());
        } else {
            view.getPasswordField().setText(view.getUnmaskedPasswordField().getText());
            view.getUnmaskedPasswordField().setVisible(false);
            view.getUnmaskedPasswordField().setManaged(false);
            view.getPasswordField().setVisible(true);
            view.getPasswordField().setManaged(true);
            view.getTogglePasswordBtn().setText("👁");
            view.getPasswordField().requestFocus();
            view.getPasswordField().positionCaret(view.getPasswordField().getText().length());
        }
    }
}

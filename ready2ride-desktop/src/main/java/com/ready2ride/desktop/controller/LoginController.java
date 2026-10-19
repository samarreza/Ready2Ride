package com.ready2ride.desktop.controller;

import com.ready2ride.desktop.view.LoginView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Controller managing user interactions on the Ready2Ride Login screen.
 * Handles input validation, password visibility toggling, and navigation callbacks.
 */
public class LoginController {

    private final LoginView view;
    private final Consumer<String> onNavigateToDashboard;
    private boolean isPasswordVisible = false;

    /**
     * Constructs the LoginController.
     *
     * @param view                   LoginView presentation instance
     * @param onNavigateToDashboard callback consumer triggered when navigating to dashboard
     */
    public LoginController(LoginView view, Consumer<String> onNavigateToDashboard) {
        this.view = Objects.requireNonNull(view, "LoginView must not be null");
        this.onNavigateToDashboard = Objects.requireNonNull(onNavigateToDashboard, "Navigation callback must not be null");

        initEventHandlers();
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

        // 2. Honest prototype behavior - notify that backend auth will be connected in Phase 3/4
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ready2Ride — Prototype Authentication Notice");
        alert.setHeaderText("Backend Authentication Scheduled for Phase 3/4");
        alert.setContentText(String.format(
                "Welcome, %s!\n\n" +
                "PostgreSQL password hashing and table-based authentication will be connected in Phase 3/4.\n" +
                "Proceeding now to the Administrator Dashboard preview.", username));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onNavigateToDashboard.accept(username);
            }
        });
    }

    private void handlePreviewDashboard() {
        view.clearError();
        // Direct demonstration navigation with default preview user
        onNavigateToDashboard.accept("Admin Preview");
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

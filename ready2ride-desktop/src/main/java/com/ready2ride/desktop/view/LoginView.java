package com.ready2ride.desktop.view;

import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

/**
 * View presentation for the Ready2Ride Administrator Login screen.
 * Implements a modern centered floating card aesthetic on a dark navy canvas,
 * constructed entirely using JavaFX controls and layout properties without CSS.
 */
public class LoginView {

    private final StackPane rootPane;
    private final TextField emailField;
    private final PasswordField passwordField;
    private final TextField unmaskedPasswordField;
    private final Button togglePasswordBtn;
    private final CheckBox rememberMeCheckBox;
    private final Button signInButton;
    private final Button previewDashboardButton;
    private final Label errorLabel;

    public LoginView() {
        // --- 1. Root Container ---
        rootPane = new StackPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_NAVY_DARK));
        rootPane.setPadding(new Insets(32));

        // --- 2. Floating Login Card ---
        VBox card = new VBox(20);
        card.setMaxWidth(430);
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 24));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 24, 1));
        card.setPadding(new Insets(38, 36, 36, 36));
        card.setEffect(UiTheme.cardShadow());

        // --- 3. Branding & Header Section ---
        StackPane logoBadge = UiTheme.createBrandLogo(54);

        Label brandLabel = new Label("Ready2Ride");
        brandLabel.setFont(UiTheme.FONT_SUBHEADER);
        brandLabel.setTextFill(UiTheme.COLOR_PRIMARY);

        HBox brandRow = new HBox(10, logoBadge, brandLabel);
        brandRow.setAlignment(Pos.CENTER);

        Label headingLabel = new Label("Welcome back");
        headingLabel.setFont(UiTheme.FONT_TITLE);
        headingLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label subtitleLabel = new Label("Please enter your administrator credentials to sign in.");
        subtitleLabel.setFont(UiTheme.FONT_BODY);
        subtitleLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);

        VBox headerBox = new VBox(8, logoBadge, headingLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // --- 4. Form Fields ---
        // Email/Username
        Label emailLabel = new Label("Email or Username");
        emailLabel.setFont(UiTheme.FONT_BODY_BOLD);
        emailLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        emailField = UiTheme.createStyledTextField("name@company.com");

        VBox emailBox = new VBox(6, emailLabel, emailField);
        emailBox.setAlignment(Pos.CENTER_LEFT);

        // Password & Toggle Visibility
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(UiTheme.FONT_BODY_BOLD);
        passwordLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label forgotPasswordPlaceholder = new Label("Forgot password?");
        forgotPasswordPlaceholder.setFont(UiTheme.FONT_CAPTION);
        forgotPasswordPlaceholder.setTextFill(UiTheme.COLOR_PRIMARY);
        forgotPasswordPlaceholder.setCursor(Cursor.HAND);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox passwordHeaderRow = new HBox(passwordLabel, spacer, forgotPasswordPlaceholder);
        passwordHeaderRow.setAlignment(Pos.CENTER_LEFT);

        passwordField = UiTheme.createStyledPasswordField("••••••••");
        unmaskedPasswordField = UiTheme.createStyledTextField("••••••••");
        unmaskedPasswordField.setVisible(false);
        unmaskedPasswordField.setManaged(false);

        // Password show/hide toggle button
        togglePasswordBtn = new Button("Show");
        togglePasswordBtn.setFont(UiTheme.FONT_CAPTION);
        togglePasswordBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        togglePasswordBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 6));
        togglePasswordBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 6, 1));
        togglePasswordBtn.setPadding(new Insets(4, 10, 4, 10));
        togglePasswordBtn.setCursor(Cursor.HAND);

        StackPane passwordInputsPane = new StackPane(passwordField, unmaskedPasswordField);
        passwordInputsPane.setAlignment(Pos.CENTER_RIGHT);

        HBox passwordFieldWithToggle = new HBox(10, passwordInputsPane, togglePasswordBtn);
        HBox.setHgrow(passwordInputsPane, Priority.ALWAYS);
        passwordFieldWithToggle.setAlignment(Pos.CENTER_LEFT);

        VBox passwordBox = new VBox(6, passwordHeaderRow, passwordFieldWithToggle);
        passwordBox.setAlignment(Pos.CENTER_LEFT);

        // Remember Me Checkbox
        rememberMeCheckBox = new CheckBox("Remember me on this workstation");
        rememberMeCheckBox.setFont(UiTheme.FONT_BODY);
        rememberMeCheckBox.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        rememberMeCheckBox.setCursor(Cursor.HAND);

        // Validation Error Message Label (initially empty)
        errorLabel = new Label();
        errorLabel.setFont(UiTheme.FONT_BODY);
        errorLabel.setTextFill(UiTheme.COLOR_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // --- 5. Action Buttons ---
        signInButton = UiTheme.createPrimaryButton("Sign in");
        signInButton.setMaxWidth(Double.MAX_VALUE);
        signInButton.setPrefHeight(44);

        // Divider
        Line lineLeft = new Line(0, 0, 110, 0);
        lineLeft.setStroke(UiTheme.COLOR_BORDER_LIGHT);
        Line lineRight = new Line(0, 0, 110, 0);
        lineRight.setStroke(UiTheme.COLOR_BORDER_LIGHT);
        Label orLabel = new Label("OR");
        orLabel.setFont(UiTheme.FONT_SMALL);
        orLabel.setTextFill(UiTheme.COLOR_TEXT_HINT);

        HBox dividerRow = new HBox(12, lineLeft, orLabel, lineRight);
        dividerRow.setAlignment(Pos.CENTER);

        previewDashboardButton = UiTheme.createSecondaryButton("Preview Dashboard →");
        previewDashboardButton.setMaxWidth(Double.MAX_VALUE);
        previewDashboardButton.setPrefHeight(42);

        // Demonstration Note
        Label demoNoticeLabel = new Label("Secure authentication: Enter administrator credentials to sign in, or select Preview Dashboard for demonstration mode.");
        demoNoticeLabel.setFont(UiTheme.FONT_SMALL_ITALIC);
        demoNoticeLabel.setTextFill(UiTheme.COLOR_TEXT_HINT);
        demoNoticeLabel.setWrapText(true);
        demoNoticeLabel.setAlignment(Pos.CENTER);

        // Footer
        Label footerLabel = new Label("Ready2Ride Fleet Management System • Admin Console");
        footerLabel.setFont(UiTheme.FONT_SMALL);
        footerLabel.setTextFill(UiTheme.COLOR_TEXT_HINT);

        // Assembly
        card.getChildren().addAll(
                headerBox,
                emailBox,
                passwordBox,
                rememberMeCheckBox,
                errorLabel,
                signInButton,
                dividerRow,
                previewDashboardButton,
                demoNoticeLabel,
                footerLabel
        );

        rootPane.getChildren().add(card);
    }

    public Parent getRootNode() {
        return rootPane;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public TextField getUnmaskedPasswordField() {
        return unmaskedPasswordField;
    }

    public Button getTogglePasswordBtn() {
        return togglePasswordBtn;
    }

    public CheckBox getRememberMeCheckBox() {
        return rememberMeCheckBox;
    }

    public Button getSignInButton() {
        return signInButton;
    }

    public Button getPreviewDashboardButton() {
        return previewDashboardButton;
    }

    public Label getErrorLabel() {
        return errorLabel;
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

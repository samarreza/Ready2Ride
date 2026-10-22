package com.ready2ride.desktop.view;

import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

/**
 * Customer Registration view matching the Ready2Ride visual identity.
 * Features a modern two-column layout with brand benefits on the left
 * and account creation form on the right.
 */
public class RegistrationView {

    private final BorderPane rootPane;
    private final TextField fullNameField;
    private final TextField emailField;
    private final TextField phoneField;
    private final TextField licenseField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;
    private final CheckBox termsCheckBox;
    private final Button registerBtn;
    private final Button backToLoginBtn;
    private final Button backToHomeBtn;
    private final Label errorLabel;
    private final Label successLabel;

    public RegistrationView(AuthenticationService authService, Runnable onNavigateToLogin, Runnable onNavigateToHome) {
        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // --- Left Brand Hero Section ---
        StackPane leftHero = new StackPane();
        leftHero.setPrefWidth(540);
        leftHero.setMinWidth(460);

        // Background scenic road image
        ImageView bgView = new ImageView();
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(leftHero.widthProperty());
        bgView.fitHeightProperty().bind(leftHero.heightProperty());

        try (InputStream in = getClass().getResourceAsStream("/images/hero_sunset.jpg")) {
            if (in != null) {
                bgView.setImage(new Image(in));
            }
        } catch (Exception ignored) {
        }

        Rectangle darkOverlay = new Rectangle();
        darkOverlay.widthProperty().bind(leftHero.widthProperty());
        darkOverlay.heightProperty().bind(leftHero.heightProperty());
        darkOverlay.setFill(Color.rgb(10, 20, 45, 0.72));

        VBox heroContent = new VBox(20);
        heroContent.setPadding(new Insets(48, 48, 48, 48));
        heroContent.setAlignment(Pos.TOP_LEFT);

        // Brand logo
        HBox brandRow = new HBox(12);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.getChildren().addAll(
                UiTheme.createBrandLogo(40),
                createBrandLabel("Ready2Ride", "RENT • RIDE • EXPLORE")
        );

        Label headline = new Label("Begin Your Journey\nWith Ready2Ride");
        headline.setFont(UiTheme.FONT_HERO_TITLE);
        headline.setTextFill(UiTheme.COLOR_WHITE);

        Label subheadline = new Label("Create your free customer account to reserve premium cars, bikes, SUVs, and vans with instant verification and best rates.");
        subheadline.setFont(UiTheme.FONT_HERO_SUBTITLE);
        subheadline.setTextFill(Color.web("#E2E8F0"));
        subheadline.setWrapText(true);

        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        // Feature list
        VBox featuresList = new VBox(14);
        featuresList.getChildren().addAll(
                createFeatureBadge(UiTheme.ICON_SHIELD, "Verified & Safe Fleet", "100% inspected vehicles with comprehensive insurance."),
                createFeatureBadge(UiTheme.ICON_RUPEE, "Transparent Pricing", "No hidden deposit fees. Transparent daily & hourly rates."),
                createFeatureBadge(UiTheme.ICON_LOCATION, "Convenient Pickups", "Across major transit hubs, colleges, and airports.")
        );

        heroContent.getChildren().addAll(brandRow, headline, subheadline, leftSpacer, featuresList);
        leftHero.getChildren().addAll(bgView, darkOverlay, heroContent);
        rootPane.setLeft(leftHero);

        // --- Right Registration Form ---
        VBox rightContainer = new VBox(16);
        rightContainer.setAlignment(Pos.CENTER);
        rightContainer.setPadding(new Insets(32, 48, 32, 48));
        rightContainer.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));

        VBox formCard = new VBox(14);
        formCard.setMaxWidth(460);
        formCard.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox navHeader = new HBox();
        backToHomeBtn = UiTheme.createGhostButton("← Back to Home");
        backToHomeBtn.setOnAction(e -> onNavigateToHome.run());
        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);
        navHeader.getChildren().addAll(backToHomeBtn, navSpacer);

        Label formTitle = new Label("Create an Account");
        formTitle.setFont(UiTheme.FONT_TITLE);
        formTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label formSubtitle = new Label("Fill in your details to start booking your rides.");
        formSubtitle.setFont(UiTheme.FONT_BODY);
        formSubtitle.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        // Fields
        fullNameField = UiTheme.createStyledTextField("Full Name (e.g. John Doe)");
        emailField = UiTheme.createStyledTextField("Email Address (name@example.com)");
        phoneField = UiTheme.createStyledTextField("Phone Number (+91 98765 43210)");
        licenseField = UiTheme.createStyledTextField("Driving License (e.g. DL-2024-98765)");

        passwordField = UiTheme.createStyledPasswordField("Password (min 6 characters)");
        confirmPasswordField = UiTheme.createStyledPasswordField("Confirm Password");

        termsCheckBox = new CheckBox("I agree to the Ready2Ride Terms of Service and Privacy Policy");
        termsCheckBox.setFont(UiTheme.FONT_CAPTION);
        termsCheckBox.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        termsCheckBox.setSelected(true);

        errorLabel = new Label();
        errorLabel.setFont(UiTheme.FONT_BODY);
        errorLabel.setTextFill(UiTheme.COLOR_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        successLabel = new Label();
        successLabel.setFont(UiTheme.FONT_BODY_BOLD);
        successLabel.setTextFill(UiTheme.COLOR_SUCCESS);
        successLabel.setWrapText(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        registerBtn = UiTheme.createPrimaryButton("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(44);

        HBox loginRow = new HBox(6);
        loginRow.setAlignment(Pos.CENTER);
        Label alreadyLabel = new Label("Already have an account?");
        alreadyLabel.setFont(UiTheme.FONT_BODY);
        alreadyLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        backToLoginBtn = new Button("Sign In");
        backToLoginBtn.setFont(UiTheme.FONT_BODY_BOLD);
        backToLoginBtn.setTextFill(UiTheme.COLOR_PRIMARY);
        backToLoginBtn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
        backToLoginBtn.setBorder(Border.EMPTY);
        backToLoginBtn.setCursor(Cursor.HAND);
        backToLoginBtn.setOnAction(e -> onNavigateToLogin.run());
        loginRow.getChildren().addAll(alreadyLabel, backToLoginBtn);

        registerBtn.setOnAction(e -> {
            clearFeedback();
            String name = fullNameField.getText() != null ? fullNameField.getText().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String license = licenseField.getText() != null ? licenseField.getText().trim() : "";
            String pass = passwordField.getText() != null ? passwordField.getText() : "";
            String confirmPass = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                showError("Please complete all required fields.");
                return;
            }

            if (!pass.equals(confirmPass)) {
                showError("Passwords do not match. Please re-enter.");
                return;
            }

            if (pass.length() < 6) {
                showError("Password must be at least 6 characters long.");
                return;
            }

            if (!termsCheckBox.isSelected()) {
                showError("You must agree to the Terms of Service to register.");
                return;
            }

            try {
                authService.registerCustomer(name, email, phone, pass, license);
                showSuccess("Account created successfully! Navigating to login...");
                registerBtn.setDisable(true);
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                pause.setOnFinished(ev -> onNavigateToLogin.run());
                pause.play();
            } catch (Exception ex) {
                showError("Registration failed: " + ex.getMessage());
            }
        });

        formCard.getChildren().addAll(
                navHeader,
                formTitle,
                formSubtitle,
                new Label("Full Name *"), fullNameField,
                new Label("Email Address *"), emailField,
                new Label("Phone Number *"), phoneField,
                new Label("Driving License"), licenseField,
                new Label("Password *"), passwordField,
                new Label("Confirm Password *"), confirmPasswordField,
                termsCheckBox,
                errorLabel,
                successLabel,
                registerBtn,
                loginRow
        );

        ScrollPane scroll = new ScrollPane(formCard);
        scroll.setFitToWidth(true);
        scroll.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        scroll.setBorder(Border.EMPTY);
        scroll.setPadding(new Insets(16, 24, 16, 24));

        rightContainer.getChildren().add(scroll);
        rootPane.setCenter(rightContainer);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void showSuccess(String msg) {
        successLabel.setText(msg);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearFeedback() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private VBox createBrandLabel(String title, String subtitle) {
        VBox box = new VBox(2);
        Label t = new Label(title);
        t.setFont(UiTheme.FONT_TITLE);
        t.setTextFill(UiTheme.COLOR_WHITE);
        Label s = new Label(subtitle);
        s.setFont(UiTheme.FONT_SMALL);
        s.setTextFill(Color.web("#93C5FD"));
        box.getChildren().addAll(t, s);
        return box;
    }

    private HBox createFeatureBadge(String iconSvg, String title, String desc) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Circle circle = new Circle(18, Color.rgb(37, 99, 235, 0.35));
        StackPane iconPane = new StackPane(circle, UiTheme.createSvgIcon(iconSvg, 18, UiTheme.COLOR_WHITE));

        VBox text = new VBox(2);
        Label t = new Label(title);
        t.setFont(UiTheme.FONT_BODY_BOLD);
        t.setTextFill(UiTheme.COLOR_WHITE);
        Label d = new Label(desc);
        d.setFont(UiTheme.FONT_CAPTION);
        d.setTextFill(Color.web("#CBD5E1"));
        text.getChildren().addAll(t, d);

        row.getChildren().addAll(iconPane, text);
        return row;
    }

    public BorderPane getRootNode() {
        return rootPane;
    }
}

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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.InputStream;

/**
 * Modern two-column Login view matching Screenshot 3 reference.
 * Features the scenic road sunset branding with 4 benefit badges on the left,
 * and a floating rounded login card with show/hide password, Remember Me,
 * Forgot Password, Create Account, and Preview Dashboard on the right.
 */
public class LoginView {

    private final BorderPane rootPane;
    private final TextField emailField;
    private final PasswordField passwordField;
    private final TextField unmaskedPasswordField;
    private final Button togglePasswordBtn;
    private final CheckBox rememberMeCheckBox;
    private final Label forgotPasswordLink;
    private final Button signInButton;
    private final VBox createAccountCard;
    private final VBox previewDashboardCard;
    private final Button backToHomeButton;
    private final Label errorLabel;

    public LoginView() {
        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // =========================================================================
        // 1. LEFT SECTION — Scenic sunset road banner with hero text & 4 badges
        // =========================================================================
        StackPane leftHero = new StackPane();
        leftHero.setPrefWidth(720);
        leftHero.setMinWidth(520);

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

        // Dark gradient overlay to ensure perfect contrast
        Rectangle darkOverlay = new Rectangle();
        darkOverlay.widthProperty().bind(leftHero.widthProperty());
        darkOverlay.heightProperty().bind(leftHero.heightProperty());
        darkOverlay.setFill(Color.rgb(10, 18, 40, 0.45));

        VBox leftContent = new VBox(22);
        leftContent.setPadding(new Insets(42, 48, 36, 48));
        leftContent.setAlignment(Pos.TOP_LEFT);

        // Top Brand Identity
        HBox brandRow = new HBox(12);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.getChildren().addAll(
                UiTheme.createBrandLogo(42),
                createBrandLabel("Ready2Ride", "RENT • RIDE • EXPLORE")
        );

        // Heading with blue highlighted word "Here"
        Text text1 = new Text("Your Next Journey\nStarts ");
        text1.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 46));
        text1.setFill(UiTheme.COLOR_WHITE);

        Text textHere = new Text("Here");
        textHere.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 46));
        textHere.setFill(UiTheme.COLOR_PRIMARY);

        TextFlow headlineFlow = new TextFlow(text1, textHere);

        Label subheadline = new Label("Rent the vehicle you need. Explore the places you love.\nSimple. Safe. Reliable.");
        subheadline.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 16));
        subheadline.setTextFill(Color.web("#F1F5F9"));
        subheadline.setLineSpacing(4);

        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        // Bottom horizontal pill with 4 circular badges (matching Screenshot 3)
        HBox benefitsPill = new HBox(20);
        benefitsPill.setAlignment(Pos.CENTER);
        benefitsPill.setBackground(UiTheme.roundedBackground(Color.rgb(15, 23, 42, 0.88), 24));
        benefitsPill.setBorder(UiTheme.strokeBorder(Color.rgb(255, 255, 255, 0.15), 24, 1));
        benefitsPill.setPadding(new Insets(16, 24, 16, 24));

        benefitsPill.getChildren().addAll(
                createHeroBenefit(UiTheme.ICON_SHIELD, "Trusted & Safe", "Verified vehicles"),
                createHeroBenefit(UiTheme.ICON_RUPEE, "Affordable Rates", "Best prices always"),
                createHeroBenefit(UiTheme.ICON_LOCATION, "Wide Locations", "Across major cities"),
                createHeroBenefit(UiTheme.ICON_HEADSET, "24/7 Support", "We're here for you")
        );

        leftContent.getChildren().addAll(brandRow, headlineFlow, subheadline, leftSpacer, benefitsPill);
        leftHero.getChildren().addAll(bgView, darkOverlay, leftContent);
        rootPane.setLeft(leftHero);

        // =========================================================================
        // 2. RIGHT SECTION — Floating rounded login card
        // =========================================================================
        StackPane rightContainer = new StackPane();
        rightContainer.setBackground(UiTheme.fillBackground(Color.web("#F0F4F8")));
        rightContainer.setPadding(new Insets(32, 40, 32, 40));

        VBox loginCard = new VBox(16);
        loginCard.setMaxWidth(440);
        loginCard.setAlignment(Pos.TOP_CENTER);
        loginCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 24));
        loginCard.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 24, 1));
        loginCard.setPadding(new Insets(34, 34, 28, 34));
        loginCard.setEffect(UiTheme.cardShadow());

        // Card Header & Logo
        HBox cardBrand = new HBox(8);
        cardBrand.setAlignment(Pos.CENTER);
        cardBrand.getChildren().addAll(
                UiTheme.createBrandLogo(36),
                createCardBrandText("Ready2Ride", "RENT • RIDE • EXPLORE")
        );

        Label welcomeTitle = new Label("Welcome Back");
        welcomeTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        welcomeTitle.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label welcomeSub = new Label("Sign in to your account and continue your journey");
        welcomeSub.setFont(UiTheme.FONT_BODY);
        welcomeSub.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        VBox headerGroup = new VBox(6, cardBrand, welcomeTitle, welcomeSub);
        headerGroup.setAlignment(Pos.CENTER);

        // Form: Username/Email
        Label emailLabel = new Label("Username or Email");
        emailLabel.setFont(UiTheme.FONT_BODY_BOLD);
        emailLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        emailField = UiTheme.createStyledTextField("Username or Email");
        emailField.setPromptText("Username or Email");

        HBox emailInputBox = new HBox(10);
        emailInputBox.setAlignment(Pos.CENTER_LEFT);
        emailInputBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        emailInputBox.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1));
        emailInputBox.setPadding(new Insets(0, 10, 0, 12));
        HBox.setHgrow(emailField, Priority.ALWAYS);
        emailField.setBorder(Border.EMPTY);
        emailField.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));

        emailInputBox.getChildren().addAll(
                UiTheme.createSvgIcon(UiTheme.ICON_USER, 16, UiTheme.COLOR_TEXT_MUTED),
                emailField
        );

        // Form: Password with show/hide eye toggle
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(UiTheme.FONT_BODY_BOLD);
        passwordLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        passwordField = UiTheme.createStyledPasswordField("Password");
        unmaskedPasswordField = UiTheme.createStyledTextField("Password");
        unmaskedPasswordField.setVisible(false);
        unmaskedPasswordField.setManaged(false);

        passwordField.setBorder(Border.EMPTY);
        passwordField.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
        unmaskedPasswordField.setBorder(Border.EMPTY);
        unmaskedPasswordField.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));

        togglePasswordBtn = new Button("👁");
        togglePasswordBtn.setFont(Font.font(14));
        togglePasswordBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        togglePasswordBtn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
        togglePasswordBtn.setBorder(Border.EMPTY);
        togglePasswordBtn.setCursor(Cursor.HAND);

        StackPane passStack = new StackPane(passwordField, unmaskedPasswordField);
        HBox.setHgrow(passStack, Priority.ALWAYS);

        HBox passwordInputBox = new HBox(8);
        passwordInputBox.setAlignment(Pos.CENTER_LEFT);
        passwordInputBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        passwordInputBox.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1));
        passwordInputBox.setPadding(new Insets(0, 10, 0, 12));
        passwordInputBox.getChildren().addAll(
                UiTheme.createSvgIcon("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z", 16, UiTheme.COLOR_TEXT_MUTED),
                passStack,
                togglePasswordBtn
        );

        // Remember me & Forgot password
        rememberMeCheckBox = new CheckBox("Remember me");
        rememberMeCheckBox.setFont(UiTheme.FONT_CAPTION);
        rememberMeCheckBox.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        rememberMeCheckBox.setCursor(Cursor.HAND);

        forgotPasswordLink = new Label("Forgot password?");
        forgotPasswordLink.setFont(UiTheme.FONT_CAPTION);
        forgotPasswordLink.setTextFill(UiTheme.COLOR_PRIMARY);
        forgotPasswordLink.setCursor(Cursor.HAND);

        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);
        HBox rememberRow = new HBox(rememberMeCheckBox, rowSpacer, forgotPasswordLink);
        rememberRow.setAlignment(Pos.CENTER_LEFT);

        // Validation Error Message
        errorLabel = new Label();
        errorLabel.setFont(UiTheme.FONT_CAPTION);
        errorLabel.setTextFill(UiTheme.COLOR_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Sign In Button
        signInButton = new Button("Sign In  →");
        signInButton.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        signInButton.setTextFill(UiTheme.COLOR_WHITE);
        signInButton.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
        signInButton.setBorder(Border.EMPTY);
        signInButton.setMaxWidth(Double.MAX_VALUE);
        signInButton.setPrefHeight(44);
        signInButton.setCursor(Cursor.HAND);

        signInButton.setOnMouseEntered(e -> signInButton.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_HOVER, 10)));
        signInButton.setOnMouseExited(e -> signInButton.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10)));

        // "or" divider
        Line l1 = new Line(0, 0, 130, 0);
        l1.setStroke(UiTheme.COLOR_BORDER_LIGHT);
        Line l2 = new Line(0, 0, 130, 0);
        l2.setStroke(UiTheme.COLOR_BORDER_LIGHT);
        Label orText = new Label("or");
        orText.setFont(UiTheme.FONT_SMALL);
        orText.setTextFill(UiTheme.COLOR_TEXT_HINT);
        HBox orDivider = new HBox(12, l1, orText, l2);
        orDivider.setAlignment(Pos.CENTER);

        // Two Action Cards side by side: "Create an Account" & "Preview Dashboard"
        createAccountCard = createActionCard(UiTheme.ICON_USER_PLUS, "Create an Account", "New to Ready2Ride?");
        previewDashboardCard = createActionCard(UiTheme.ICON_USER, "Preview Dashboard", "Explore without login");

        HBox cardsRow = new HBox(12, createAccountCard, previewDashboardCard);
        cardsRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(createAccountCard, Priority.ALWAYS);
        HBox.setHgrow(previewDashboardCard, Priority.ALWAYS);

        // Back to customer home link
        backToHomeButton = UiTheme.createGhostButton("← Back to Home");
        backToHomeButton.setMaxWidth(Double.MAX_VALUE);

        // Card Bottom Footer & Carousel Dots
        Label footerNote = new Label("Make Every Journey Memorable");
        footerNote.setFont(UiTheme.FONT_SMALL);
        footerNote.setTextFill(UiTheme.COLOR_TEXT_HINT);

        HBox dotsRow = new HBox(6);
        dotsRow.setAlignment(Pos.CENTER);
        Circle dot1 = new Circle(4, UiTheme.COLOR_PRIMARY);
        Circle dot2 = new Circle(4, Color.web("#CBD5E1"));
        Circle dot3 = new Circle(4, Color.web("#CBD5E1"));
        dotsRow.getChildren().addAll(dot1, dot2, dot3);

        VBox footerBox = new VBox(6, footerNote, dotsRow);
        footerBox.setAlignment(Pos.CENTER);

        // Assembly
        loginCard.getChildren().addAll(
                headerGroup,
                emailInputBox,
                passwordInputBox,
                rememberRow,
                errorLabel,
                signInButton,
                orDivider,
                cardsRow,
                backToHomeButton,
                footerBox
        );

        rightContainer.getChildren().add(loginCard);
        rootPane.setCenter(rightContainer);
    }

    private VBox createActionCard(String iconSvg, String title, String subtitle) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 12));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        card.setCursor(Cursor.HAND);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER);
        top.getChildren().addAll(
                UiTheme.createSvgIcon(iconSvg, 16, UiTheme.COLOR_PRIMARY),
                new Label(title) {{
                    setFont(UiTheme.FONT_BODY_BOLD);
                    setTextFill(UiTheme.COLOR_PRIMARY);
                }}
        );

        Label sub = new Label(subtitle);
        sub.setFont(UiTheme.FONT_SMALL);
        sub.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        card.getChildren().addAll(top, sub);

        card.setOnMouseEntered(e -> {
            card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 12));
            card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 12, 1));
        });
        card.setOnMouseExited(e -> {
            card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 12));
            card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        });

        return card;
    }

    private VBox createHeroBenefit(String iconSvg, String title, String subtitle) {
        VBox col = new VBox(4);
        col.setAlignment(Pos.CENTER);
        col.setPrefWidth(125);

        Circle circle = new Circle(18, Color.rgb(255, 255, 255, 0.15));
        StackPane iconPane = new StackPane(circle, UiTheme.createSvgIcon(iconSvg, 18, UiTheme.COLOR_WHITE));

        Label t = new Label(title);
        t.setFont(UiTheme.FONT_BODY_BOLD);
        t.setTextFill(UiTheme.COLOR_WHITE);

        Label s = new Label(subtitle);
        s.setFont(UiTheme.FONT_SMALL);
        s.setTextFill(Color.web("#94A3B8"));

        col.getChildren().addAll(iconPane, t, s);
        return col;
    }

    private VBox createBrandLabel(String title, String tagline) {
        VBox box = new VBox(2);
        Label t = new Label(title);
        t.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 22));
        t.setTextFill(UiTheme.COLOR_WHITE);
        Label tag = new Label(tagline);
        tag.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 9));
        tag.setTextFill(Color.web("#93C5FD"));
        box.getChildren().addAll(t, tag);
        return box;
    }

    private VBox createCardBrandText(String title, String tagline) {
        VBox box = new VBox(1);
        Label t = new Label(title);
        t.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        t.setTextFill(UiTheme.COLOR_NAVY_DARK);
        Label tag = new Label(tagline);
        tag.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 8));
        tag.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        box.getChildren().addAll(t, tag);
        return box;
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

    public Label getForgotPasswordLink() {
        return forgotPasswordLink;
    }

    public Button getSignInButton() {
        return signInButton;
    }

    public VBox getCreateAccountCard() {
        return createAccountCard;
    }

    public VBox getPreviewDashboardCard() {
        return previewDashboardCard;
    }

    public Button getBackToHomeButton() {
        return backToHomeButton;
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

package com.ready2ride.desktop.view;

import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.Objects;

/**
 * Dedicated JavaFX View for the Ready2Ride "About Us" page.
 * Implements the comprehensive Ready2Ride design language with:
 * <ul>
 *     <li>Shared Top Navigation Bar (with active About Us pill and Contact removed).</li>
 *     <li>Hero section with scenic road background, high-contrast overlay, heading and subtitle.</li>
 *     <li>Our Story section detailing customer and rental provider connection.</li>
 *     <li>Our Mission section with core pillars (Simple, Convenient, Transparent, Accessible).</li>
 *     <li>Why Choose Ready2Ride card grid (Wide Selection, Affordable Rates, Simple Booking, Trusted Experience).</li>
 *     <li>How It Works 4-step workflow (Browse -> Select -> Book & Confirm -> Pick Up & Ride).</li>
 *     <li>For Rental Providers section detailing listing, photo uploads, pricing, and inventory management.</li>
 *     <li>Direct "Explore Vehicles" call-to-action button opening the vehicle catalog.</li>
 *     <li>Consistent branded footer.</li>
 * </ul>
 */
public class AboutUsView {

    private final BorderPane rootPane;
    private final ScrollPane scrollPane;
    private final TopNavBar topNavBar;

    private final Button heroExploreBtn;
    private final Button ctaExploreBtn;

    private final Runnable onNavigateHome;
    private final Runnable onNavigateVehicles;
    private final Runnable onNavigateMyBookings;
    private final Runnable onOpenLogin;
    private final Runnable onOpenRegister;

    public AboutUsView(Runnable onNavigateHome,
                       Runnable onNavigateVehicles,
                       Runnable onNavigateMyBookings,
                       Runnable onOpenLogin,
                       Runnable onOpenRegister) {
        this.onNavigateHome = onNavigateHome;
        this.onNavigateVehicles = onNavigateVehicles != null ? onNavigateVehicles : () -> {};
        this.onNavigateMyBookings = onNavigateMyBookings;
        this.onOpenLogin = onOpenLogin;
        this.onOpenRegister = onOpenRegister;

        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        scrollPane.setBorder(Border.EMPTY);

        // 1. Top Navigation Bar (Active: ABOUT)
        topNavBar = new TopNavBar(TopNavBar.NavPage.ABOUT);
        topNavBar.setOnNavigateHome(onNavigateHome);
        topNavBar.setOnNavigateVehicles(this.onNavigateVehicles);
        topNavBar.setOnNavigateMyBookings(onNavigateMyBookings);
        topNavBar.setOnNavigateAboutUs(() -> scrollPane.setVvalue(0));
        topNavBar.setOnLogin(onOpenLogin);
        topNavBar.setOnRegister(onOpenRegister);
        topNavBar.setOnLogout(onOpenLogin);

        rootPane.setTop(topNavBar);

        // Buttons for catalog navigation
        heroExploreBtn = UiTheme.createPrimaryButton("Explore Vehicles →");
        heroExploreBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 14));
        heroExploreBtn.setPadding(new Insets(12, 26, 12, 26));
        heroExploreBtn.setOnAction(e -> this.onNavigateVehicles.run());

        ctaExploreBtn = UiTheme.createPrimaryButton("Explore Vehicles Now →");
        ctaExploreBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        ctaExploreBtn.setPadding(new Insets(13, 30, 13, 30));
        ctaExploreBtn.setOnAction(e -> this.onNavigateVehicles.run());

        // 2. Main Scrollable Container
        VBox contentBox = new VBox(0);
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setFillWidth(true);

        // Section A: Hero Section
        StackPane heroSection = buildHeroSection();

        // Section B: Body Content Container
        VBox bodyContent = new VBox(52);
        bodyContent.setAlignment(Pos.TOP_CENTER);
        bodyContent.setPadding(new Insets(48, 60, 56, 60));
        bodyContent.setMaxWidth(1280);

        // Add all required sections
        bodyContent.getChildren().addAll(
                buildStoryAndMissionSection(),
                buildWhyChooseUsSection(),
                buildHowItWorksSection(),
                buildRentalProvidersSection(),
                buildCallToActionBanner()
        );

        // Section C: Footer
        VBox footer = buildFooter();

        contentBox.getChildren().addAll(heroSection, bodyContent, footer);
        scrollPane.setContent(contentBox);

        rootPane.setCenter(scrollPane);
    }

    public Parent getRootNode() {
        return rootPane;
    }

    public TopNavBar getTopNavBar() {
        return topNavBar;
    }

    public Button getHeroExploreBtn() {
        return heroExploreBtn;
    }

    public Button getCtaExploreBtn() {
        return ctaExploreBtn;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    // =========================================================================
    // SECTION 1: HERO SECTION
    // =========================================================================
    private StackPane buildHeroSection() {
        StackPane heroStack = new StackPane();
        heroStack.setPrefHeight(320);
        heroStack.setMinHeight(280);
        heroStack.setMaxHeight(360);
        heroStack.setAlignment(Pos.CENTER_LEFT);

        // Background scenic road image
        ImageView heroBg = new ImageView();
        Image heroImg = loadResourceImage("hero_sunset.jpg");
        if (heroImg != null) {
            heroBg.setImage(heroImg);
        }
        heroBg.setPreserveRatio(false);
        heroBg.fitWidthProperty().bind(rootPane.widthProperty());
        heroBg.setFitHeight(320);

        // High-contrast gradient overlay ensuring readability
        Rectangle gradientOverlay = new Rectangle();
        gradientOverlay.widthProperty().bind(rootPane.widthProperty());
        gradientOverlay.setHeight(320);
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(15, 23, 42, 0.92)),
                new Stop(0.45, Color.rgb(15, 23, 42, 0.82)),
                new Stop(0.80, Color.rgb(15, 23, 42, 0.45)),
                new Stop(1.0, Color.rgb(15, 23, 42, 0.25))
        );
        gradientOverlay.setFill(gradient);

        // Foreground text and action
        VBox textContainer = new VBox(12);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setPadding(new Insets(30, 80, 30, 80));
        textContainer.setMaxWidth(1280);

        // Badge pill
        Label badge = new Label("ABOUT US");
        badge.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
        badge.setTextFill(Color.web("#93C5FD"));
        badge.setBackground(UiTheme.roundedBackground(Color.rgb(37, 99, 235, 0.35), 12));
        badge.setBorder(UiTheme.strokeBorder(Color.web("#3B82F6"), 12, 1));
        badge.setPadding(new Insets(4, 12, 4, 12));

        // Heading
        Label heading = new Label("About Ready2Ride");
        heading.setFont(UiTheme.FONT_HERO_TITLE);
        heading.setTextFill(UiTheme.COLOR_WHITE);

        // Subtitle
        Label subtitle = new Label("Making every journey simple, safe, and memorable.");
        subtitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 18));
        subtitle.setTextFill(Color.web("#E2E8F0"));

        HBox buttonRow = new HBox(16, heroExploreBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.setPadding(new Insets(8, 0, 0, 0));

        textContainer.getChildren().addAll(badge, heading, subtitle, buttonRow);

        heroStack.getChildren().addAll(heroBg, gradientOverlay, textContainer);
        return heroStack;
    }

    // =========================================================================
    // SECTION 2: OUR STORY & OUR MISSION
    // =========================================================================
    private VBox buildStoryAndMissionSection() {
        VBox section = new VBox(28);
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(1260);

        // Section Title Header
        VBox headerBox = createSectionHeader("OUR PURPOSE", "Our Story & Mission",
                "Connecting customers with dependable vehicles and empowering local rental providers.");

        // Two-column layout: Left = Our Story, Right = Our Mission Card
        HBox columnsBox = new HBox(32);
        columnsBox.setAlignment(Pos.TOP_LEFT);

        // Left Column: Our Story
        VBox storyBox = new VBox(16);
        storyBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(storyBox, Priority.ALWAYS);

        Label storyTitle = new Label("Connecting Riders & Rental Providers");
        storyTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 20));
        storyTitle.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label p1 = new Label("Ready2Ride was founded as a dedicated vehicle rental platform designed to solve a fundamental transportation challenge: connecting customers seeking convenient, flexible travel with trusted rental providers offering well-maintained fleets.");
        p1.setFont(Font.font(UiTheme.FONT_FAMILY, 14));
        p1.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        p1.setWrapText(true);
        p1.setLineSpacing(3);

        Label p2 = new Label("Whether customers require an economical bike for quick city commutes, a comfortable sedan for family visits, a rugged SUV for weekend getaways, or a spacious passenger van for group journeys, Ready2Ride bridges the gap with a direct, streamlined rental process.");
        p2.setFont(Font.font(UiTheme.FONT_FAMILY, 14));
        p2.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        p2.setWrapText(true);
        p2.setLineSpacing(3);

        Label p3 = new Label("At the same time, we give independent rental providers a robust dashboard to showcase their vehicles, configure competitive pricing, upload photos, and manage daily rental inventory effortlessly.");
        p3.setFont(Font.font(UiTheme.FONT_FAMILY, 14));
        p3.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        p3.setWrapText(true);
        p3.setLineSpacing(3);

        storyBox.getChildren().addAll(storyTitle, p1, p2, p3);

        // Right Column: Our Mission Card (Highlighted)
        VBox missionCard = new VBox(18);
        missionCard.setAlignment(Pos.TOP_LEFT);
        missionCard.setPrefWidth(480);
        missionCard.setMinWidth(420);
        missionCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_DARK, 18));
        missionCard.setPadding(new Insets(28, 30, 28, 30));
        missionCard.setEffect(UiTheme.cardShadow());

        Label missionBadge = new Label("OUR MISSION");
        missionBadge.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
        missionBadge.setTextFill(UiTheme.COLOR_PRIMARY);
        missionBadge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 10));
        missionBadge.setPadding(new Insets(3, 10, 3, 10));

        Label missionHeading = new Label("Make vehicle rentals simple, convenient, transparent, and accessible.");
        missionHeading.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        missionHeading.setTextFill(UiTheme.COLOR_WHITE);
        missionHeading.setWrapText(true);
        missionHeading.setLineSpacing(2);

        VBox pillarsBox = new VBox(12);
        pillarsBox.getChildren().addAll(
                createMissionPillar("Simple", "Intuitive digital booking process with instant confirmation and no paper hassles."),
                createMissionPillar("Convenient", "Flexible pickup hubs, clear dates selection, and easy booking lookup."),
                createMissionPillar("Transparent", "Upfront daily rental rates in INR with zero unexpected fees or surprises."),
                createMissionPillar("Accessible", "Diverse vehicles suited for every budget, travel group size, and destination.")
        );

        missionCard.getChildren().addAll(missionBadge, missionHeading, pillarsBox);

        columnsBox.getChildren().addAll(storyBox, missionCard);
        section.getChildren().addAll(headerBox, columnsBox);
        return section;
    }

    private HBox createMissionPillar(String title, String desc) {
        Circle dot = new Circle(4, UiTheme.COLOR_PRIMARY);
        dot.setTranslateY(6);

        Label t = new Label(title + ": ");
        t.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 13));
        t.setTextFill(UiTheme.COLOR_WHITE);

        Label d = new Label(desc);
        d.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 13));
        d.setTextFill(Color.web("#94A3B8"));
        d.setWrapText(true);

        HBox textBox = new HBox(4, t, d);
        textBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox row = new HBox(10, dot, textBox);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    // =========================================================================
    // SECTION 3: WHY CHOOSE READY2RIDE
    // =========================================================================
    private VBox buildWhyChooseUsSection() {
        VBox section = new VBox(24);
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(1260);

        VBox headerBox = createSectionHeader("KEY ADVANTAGES", "Why Choose Ready2Ride",
                "Built to deliver an unmatched rental experience for everyday riders and road-trippers.");

        FlowPane cardsPane = new FlowPane();
        cardsPane.setHgap(20);
        cardsPane.setVgap(20);
        cardsPane.setPrefWrapLength(1260);

        cardsPane.getChildren().addAll(
                createBenefitCard(
                        UiTheme.ICON_CAR,
                        UiTheme.COLOR_PRIMARY,
                        "Wide Vehicle Selection",
                        "Choose from diverse self-drive options including economical bikes, compact cars, premium sedans, high-clearance SUVs, and spacious passenger vans."
                ),
                createBenefitCard(
                        UiTheme.ICON_RUPEE,
                        Color.web("#059669"), // Emerald green
                        "Affordable Rental Rates",
                        "Competitive, fixed-precision daily rental rates shown clearly in Indian Rupees (INR) with complete pricing transparency and no hidden fees."
                ),
                createBenefitCard(
                        UiTheme.ICON_CALENDAR,
                        Color.web("#7C3AED"), // Royal purple
                        "Simple Booking Process",
                        "Streamlined 4-step reservation journey with instant availability checks, calendar date pickers, pickup location selection, and instant confirmations."
                ),
                createBenefitCard(
                        UiTheme.ICON_SHIELD,
                        Color.web("#D97706"), // Amber/Gold
                        "Trusted and Convenient Experience",
                        "Verified rental provider inventory, customer profile security, self-service cancellation management, and reliable customer support."
                )
        );

        section.getChildren().addAll(headerBox, cardsPane);
        return section;
    }

    private VBox createBenefitCard(String iconPath, Color accentColor, String title, String description) {
        VBox card = new VBox(14);
        card.setPrefWidth(285);
        card.setMinWidth(260);
        card.setMaxWidth(295);
        card.setPrefHeight(230);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        card.setEffect(UiTheme.subtleShadow());
        card.setPadding(new Insets(24, 22, 24, 22));

        // Circular icon container
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(48, 48);
        iconCircle.setMinSize(48, 48);
        iconCircle.setMaxSize(48, 48);
        iconCircle.setBackground(UiTheme.roundedBackground(Color.rgb((int)(accentColor.getRed()*255), (int)(accentColor.getGreen()*255), (int)(accentColor.getBlue()*255), 0.12), 24));
        iconCircle.getChildren().add(UiTheme.createSvgIcon(iconPath, 22, accentColor));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        titleLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);
        titleLabel.setWrapText(true);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(UiTheme.FONT_FAMILY, 13));
        descLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        descLabel.setWrapText(true);
        descLabel.setLineSpacing(2);

        card.getChildren().addAll(iconCircle, titleLabel, descLabel);

        // Interactive hover elevation
        card.setOnMouseEntered(e -> {
            card.setEffect(UiTheme.cardShadow());
            card.setBorder(UiTheme.strokeBorder(accentColor, 16, 1));
        });
        card.setOnMouseExited(e -> {
            card.setEffect(UiTheme.subtleShadow());
            card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        });

        return card;
    }

    // =========================================================================
    // SECTION 4: HOW IT WORKS
    // =========================================================================
    private VBox buildHowItWorksSection() {
        VBox section = new VBox(24);
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(1260);

        VBox headerBox = createSectionHeader("CUSTOMER JOURNEY", "How It Works",
                "From selecting your dream vehicle to returning from the ride in four intuitive steps.");

        HBox stepsContainer = new HBox(12);
        stepsContainer.setAlignment(Pos.CENTER);
        stepsContainer.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        stepsContainer.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        stepsContainer.setEffect(UiTheme.subtleShadow());
        stepsContainer.setPadding(new Insets(32, 28, 32, 28));

        HBox step1 = createWorkflowStep(1, UiTheme.ICON_SEARCH, "Browse Vehicles",
                "Explore our complete fleet filtered by vehicle type, daily rates, fuel, and seating capacity.");
        Label arrow1 = createStepArrow();

        HBox step2 = createWorkflowStep(2, UiTheme.ICON_CAR, "Select a Vehicle",
                "Review photos, key specifications, daily rental pricing, and available pickup locations.");
        Label arrow2 = createStepArrow();

        HBox step3 = createWorkflowStep(3, UiTheme.ICON_CALENDAR, "Book & Confirm",
                "Select your trip dates, specify pickup depot, confirm booking details, and receive instant confirmation.");
        Label arrow3 = createStepArrow();

        HBox step4 = createWorkflowStep(4, UiTheme.ICON_STEERING, "Pick Up & Ride",
                "Head to the designated location, collect keys, and start your safe and memorable journey!");

        HBox.setHgrow(step1, Priority.ALWAYS);
        HBox.setHgrow(step2, Priority.ALWAYS);
        HBox.setHgrow(step3, Priority.ALWAYS);
        HBox.setHgrow(step4, Priority.ALWAYS);

        stepsContainer.getChildren().addAll(step1, arrow1, step2, arrow2, step3, arrow3, step4);

        section.getChildren().addAll(headerBox, stepsContainer);
        return section;
    }

    private HBox createWorkflowStep(int stepNum, String iconPath, String title, String desc) {
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(56, 56);
        iconCircle.setMinSize(56, 56);
        iconCircle.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 28));
        iconCircle.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 28, 1.5));
        iconCircle.setEffect(UiTheme.subtleShadow());
        iconCircle.getChildren().add(UiTheme.createSvgIcon(iconPath, 24, UiTheme.COLOR_PRIMARY));

        Label numBadge = new Label(String.valueOf(stepNum));
        numBadge.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 10));
        numBadge.setTextFill(UiTheme.COLOR_WHITE);
        numBadge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 9));
        numBadge.setPadding(new Insets(2, 6, 2, 6));

        StackPane stepIconWithBadge = new StackPane(iconCircle, numBadge);
        StackPane.setAlignment(numBadge, Pos.TOP_LEFT);
        numBadge.setTranslateX(-2);
        numBadge.setTranslateY(-2);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        titleLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font(UiTheme.FONT_FAMILY, 12));
        descLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        descLabel.setWrapText(true);
        descLabel.setLineSpacing(2);

        VBox textBox = new VBox(3, titleLabel, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox stepBox = new HBox(12, stepIconWithBadge, textBox);
        stepBox.setAlignment(Pos.CENTER_LEFT);
        return stepBox;
    }

    private Label createStepArrow() {
        Label arrow = new Label("→");
        arrow.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 20));
        arrow.setTextFill(UiTheme.COLOR_PRIMARY);
        arrow.setPadding(new Insets(0, 4, 0, 4));
        return arrow;
    }

    // =========================================================================
    // SECTION 5: FOR RENTAL PROVIDERS
    // =========================================================================
    private VBox buildRentalProvidersSection() {
        VBox section = new VBox(24);
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(1260);

        VBox headerBox = createSectionHeader("PARTNERSHIP", "For Rental Providers",
                "Powerful tools for fleet owners to showcase vehicles, manage rates, and oversee operations.");

        // Container with light background and 4 provider capabilities
        VBox providerContainer = new VBox(24);
        providerContainer.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 18));
        providerContainer.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 18, 1));
        providerContainer.setEffect(UiTheme.subtleShadow());
        providerContainer.setPadding(new Insets(32, 34, 32, 34));

        Label introLabel = new Label("Rental providers form the foundation of the Ready2Ride marketplace. Through our dedicated Rental Provider Dashboard, partners gain total control over their business operations:");
        introLabel.setFont(Font.font(UiTheme.FONT_FAMILY, 14));
        introLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);
        introLabel.setWrapText(true);

        FlowPane featureGrid = new FlowPane();
        featureGrid.setHgap(20);
        featureGrid.setVgap(20);
        featureGrid.setPrefWrapLength(1200);

        featureGrid.getChildren().addAll(
                createProviderFeatureCard(
                        "List Vehicles",
                        "Easily register cars, bikes, SUVs, and passenger vans with exact make, model, year, transmission, registration number, and seating specifications."
                ),
                createProviderFeatureCard(
                        "Upload Images",
                        "Upload genuine vehicle photos so customers can inspect actual interior and exterior condition prior to reserving."
                ),
                createProviderFeatureCard(
                        "Manage Rental Prices",
                        "Set, adjust, and update daily rental rates dynamically in INR based on seasonal demand, duration, and fleet maintenance."
                ),
                createProviderFeatureCard(
                        "Manage Fleet Inventory",
                        "Track real-time vehicle availability, review ongoing customer bookings, and schedule routine maintenance or servicing directly in the dashboard."
                )
        );

        // Note on accessing Provider Dashboard
        HBox noteBox = new HBox(12);
        noteBox.setAlignment(Pos.CENTER_LEFT);
        noteBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 10));
        noteBox.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 10, 1));
        noteBox.setPadding(new Insets(12, 18, 12, 18));

        Label infoIcon = new Label("💡");
        infoIcon.setFont(Font.font(16));

        Label noteText = new Label("Fleet owners and rental partners can access these comprehensive management capabilities by logging into the application with their Rental Provider credentials.");
        noteText.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        noteText.setTextFill(UiTheme.COLOR_PRIMARY);
        noteText.setWrapText(true);
        HBox.setHgrow(noteText, Priority.ALWAYS);

        noteBox.getChildren().addAll(infoIcon, noteText);

        providerContainer.getChildren().addAll(introLabel, featureGrid, noteBox);
        section.getChildren().addAll(headerBox, providerContainer);
        return section;
    }

    private VBox createProviderFeatureCard(String title, String desc) {
        VBox card = new VBox(8);
        card.setPrefWidth(265);
        card.setMinWidth(240);
        card.setMaxWidth(285);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 12));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        card.setPadding(new Insets(18, 18, 18, 18));

        Label checkmark = new Label("✓  " + title);
        checkmark.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        checkmark.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font(UiTheme.FONT_FAMILY, 13));
        descLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        descLabel.setWrapText(true);
        descLabel.setLineSpacing(2);

        card.getChildren().addAll(checkmark, descLabel);
        return card;
    }

    // =========================================================================
    // SECTION 6: CALL TO ACTION BANNER (EXPLORE VEHICLES)
    // =========================================================================
    private StackPane buildCallToActionBanner() {
        StackPane banner = new StackPane();
        banner.setMaxWidth(1260);
        banner.setPrefHeight(160);
        banner.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_DARK, 18));
        banner.setEffect(UiTheme.cardShadow());
        banner.setPadding(new Insets(30, 48, 30, 48));

        HBox content = new HBox(32);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(6);
        text.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label title = new Label("Ready to Start Your Journey?");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(UiTheme.COLOR_WHITE);

        Label subtitle = new Label("Browse our verified vehicle catalog and rent the car, bike, SUV, or van that fits your plans perfectly.");
        subtitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#CBD5E1"));
        subtitle.setWrapText(true);

        text.getChildren().addAll(title, subtitle);

        content.getChildren().addAll(text, ctaExploreBtn);
        banner.getChildren().add(content);
        return banner;
    }

    // =========================================================================
    // SECTION 7: FOOTER
    // =========================================================================
    private VBox buildFooter() {
        VBox footer = new VBox(20);
        footer.setBackground(UiTheme.fillBackground(UiTheme.COLOR_NAVY_DARK));
        footer.setPadding(new Insets(36, 60, 24, 60));
        footer.setAlignment(Pos.CENTER);

        HBox topRow = new HBox(40);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setMaxWidth(1260);

        Label brandTitle = new Label("Ready2Ride");
        brandTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 20));
        brandTitle.setTextFill(UiTheme.COLOR_WHITE);

        Label tagline = new Label("Rent the vehicle you need. Explore the places you love.");
        tagline.setFont(UiTheme.FONT_CAPTION);
        tagline.setTextFill(UiTheme.COLOR_TEXT_HINT);

        VBox brandBox = new VBox(4, brandTitle, tagline);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Label phone = new Label("📞  24/7 Helpline: +91-1800-RIDE-NOW");
        phone.setFont(UiTheme.FONT_CAPTION);
        phone.setTextFill(UiTheme.COLOR_TEXT_LIGHT);

        Label email = new Label("✉  support@ready2ride.internal");
        email.setFont(UiTheme.FONT_CAPTION);
        email.setTextFill(UiTheme.COLOR_TEXT_HINT);

        VBox contactBox = new VBox(4, phone, email);
        contactBox.setAlignment(Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(brandBox, fSpacer, contactBox);

        Line divider = new Line(0, 0, 1260, 0);
        divider.setStroke(Color.rgb(255, 255, 255, 0.10));

        Label copyright = new Label("© 2026 Ready2Ride Vehicle Rental Management System • All rights reserved.");
        copyright.setFont(UiTheme.FONT_SMALL);
        copyright.setTextFill(UiTheme.COLOR_TEXT_HINT);

        footer.getChildren().addAll(topRow, divider, copyright);
        return footer;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private VBox createSectionHeader(String badgeText, String titleText, String subtitleText) {
        VBox header = new VBox(6);
        header.setAlignment(Pos.TOP_LEFT);

        Label badge = new Label(badgeText);
        badge.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
        badge.setTextFill(UiTheme.COLOR_PRIMARY);
        badge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8));
        badge.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1));
        badge.setPadding(new Insets(3, 8, 3, 8));

        Label title = new Label(titleText);
        title.setFont(UiTheme.FONT_SECTION_TITLE);
        title.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label sub = new Label(subtitleText);
        sub.setFont(UiTheme.FONT_SECTION_SUB);
        sub.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        header.getChildren().addAll(badge, title, sub);
        return header;
    }

    private Image loadResourceImage(String fileName) {
        try (InputStream in = getClass().getResourceAsStream("/images/" + fileName)) {
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

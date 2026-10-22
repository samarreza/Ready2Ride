package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleType;
import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX View presentation for the Ready2Ride Home Page.
 * Closely reproduces the reference landing page screenshot:
 * <ul>
 *     <li>Navigation header with Ready2Ride logo, active Home pill, menu links, and login/register actions.</li>
 *     <li>Scenic hero banner with mountain sunset winding road and luxury car.</li>
 *     <li>Prominent floating booking panel with "Rent a Vehicle" and "Find My Booking" tabs.</li>
 *     <li>Dark translucent benefits strip (Trusted & Safe, Affordable Rates, 24/7 Support, Wide Locations).</li>
 *     <li>"Choose Your Ride" four category cards (Cars, Bikes, SUVs, Vans) with prices and images loaded from PostgreSQL.</li>
 *     <li>"How It Works" four-step numbered workflow with connecting arrows.</li>
 *     <li>Modern branded footer.</li>
 * </ul>
 */
public class HomeView {

    private final BorderPane rootPane;
    private final ScrollPane scrollPane;

    // Header buttons
    private final HBox brandLogoBox;
    private final Button navHomeBtn;
    private final Button navVehiclesBtn;
    private final Button navHowItWorksBtn;
    private final Button navAboutBtn;
    private final Button navContactBtn;
    private final Button navSearchBtn;
    private final Button navLoginBtn;
    private final Button navRegisterBtn;

    // Booking Panel controls
    private final Button tabRentVehicleBtn;
    private final Button tabFindBookingBtn;
    private final ComboBox<String> locationComboBox;
    private final DatePicker pickupDatePicker;
    private final DatePicker returnDatePicker;
    private final Button searchVehiclesBtn;
    private final Label bookingValidationLabel;

    // Find My Booking tab controls
    private final TextField bookingLookupField;
    private final Button bookingLookupBtn;
    private final Label bookingLookupResultLabel;
    private final Button bookingCancelBtn;
    private final VBox rentTabContentBox;
    private final VBox findBookingContentBox;

    // Category Cards controls & references
    private final Hyperlink viewAllVehiclesLink;
    private final Map<VehicleType, CategoryCardComponent> categoryCards = new HashMap<>();

    // Section jump target references
    private final VBox howItWorksSectionBox;
    private final VBox chooseRideSectionBox;

    public HomeView() {
        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // =====================================================================
        // SECTION A: Navigation Header
        // =====================================================================
        HBox header = new HBox(16);
        header.setPrefHeight(72);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 36, 10, 36));
        header.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        header.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 0, 1));
        header.setEffect(UiTheme.subtleShadow());

        // Left brand logo
        brandLogoBox = UiTheme.createBrandHeaderLogo();

        // Center navigation pills container
        HBox centerNav = new HBox(6);
        centerNav.setAlignment(Pos.CENTER);
        centerNav.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 24));
        centerNav.setPadding(new Insets(4, 6, 4, 6));

        navHomeBtn = createHeaderNavPill("Home", UiTheme.ICON_HOME, true);
        navVehiclesBtn = createHeaderNavPill("Vehicles", UiTheme.ICON_CAR, false);
        navHowItWorksBtn = createHeaderNavPill("How It Works", UiTheme.ICON_HOW_IT_WORKS, false);
        navAboutBtn = createHeaderNavPill("About Us", UiTheme.ICON_INFO, false);
        navContactBtn = createHeaderNavPill("Contact", UiTheme.ICON_PHONE, false);

        centerNav.getChildren().addAll(navHomeBtn, navVehiclesBtn, navHowItWorksBtn, navAboutBtn, navContactBtn);

        // Right side action buttons
        navSearchBtn = new Button();
        navSearchBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_SEARCH, 18, UiTheme.COLOR_TEXT_DARK));
        navSearchBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
        navSearchBtn.setPadding(new Insets(8));
        navSearchBtn.setCursor(Cursor.HAND);
        navSearchBtn.setOnMouseEntered(e -> navSearchBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 20)));
        navSearchBtn.setOnMouseExited(e -> navSearchBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20)));

        navLoginBtn = new Button("Login");
        navLoginBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_USER, 16, UiTheme.COLOR_PRIMARY));
        navLoginBtn.setFont(UiTheme.FONT_BODY_BOLD);
        navLoginBtn.setTextFill(UiTheme.COLOR_PRIMARY);
        navLoginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        navLoginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1.5));
        navLoginBtn.setPadding(new Insets(8, 20, 8, 16));
        navLoginBtn.setCursor(Cursor.HAND);
        navLoginBtn.setOnMouseEntered(e -> {
            navLoginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8));
            navLoginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY, 8, 1.5));
        });
        navLoginBtn.setOnMouseExited(e -> {
            navLoginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            navLoginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1.5));
        });

        navRegisterBtn = new Button("Register");
        navRegisterBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_USER_PLUS, 16, UiTheme.COLOR_WHITE));
        navRegisterBtn.setFont(UiTheme.FONT_BODY_BOLD);
        navRegisterBtn.setTextFill(UiTheme.COLOR_WHITE);
        navRegisterBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
        navRegisterBtn.setBorder(Border.EMPTY);
        navRegisterBtn.setPadding(new Insets(9, 22, 9, 18));
        navRegisterBtn.setCursor(Cursor.HAND);
        navRegisterBtn.setOnMouseEntered(e -> navRegisterBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_HOVER, 8)));
        navRegisterBtn.setOnMouseExited(e -> navRegisterBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8)));

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox rightActions = new HBox(12, navSearchBtn, navLoginBtn, navRegisterBtn);
        rightActions.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(brandLogoBox, spacer1, centerNav, spacer2, rightActions);
        rootPane.setTop(header);

        // =====================================================================
        // SECTION B: Hero, Booking Panel, and Benefits Strip
        // =====================================================================
        VBox scrollContent = new VBox(0);
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setFillWidth(true);

        StackPane heroStack = new StackPane();
        heroStack.setPrefHeight(560);
        heroStack.setMinHeight(520);
        heroStack.setMaxHeight(600);
        heroStack.setAlignment(Pos.TOP_CENTER);

        // 1. Scenic sunset mountain image background
        ImageView heroBg = new ImageView();
        Image heroImage = loadLocalResourceImage("hero_sunset.jpg");
        if (heroImage != null) {
            heroBg.setImage(heroImage);
        }
        heroBg.setPreserveRatio(false);
        heroBg.fitWidthProperty().bind(rootPane.widthProperty());
        heroBg.setFitHeight(560);

        // 2. High-contrast gradient overlay to ensure text readability on left
        Rectangle gradientOverlay = new Rectangle();
        gradientOverlay.widthProperty().bind(rootPane.widthProperty());
        gradientOverlay.setHeight(560);
        LinearGradient leftGradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 255, 255, 0.88)),
                new Stop(0.35, Color.rgb(255, 255, 255, 0.72)),
                new Stop(0.65, Color.rgb(255, 255, 255, 0.15)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.10))
        );
        gradientOverlay.setFill(leftGradient);

        // 3. Hero content layout: Title, Subtitle, Booking Panel, Benefits
        VBox heroForeground = new VBox(18);
        heroForeground.setAlignment(Pos.TOP_LEFT);
        heroForeground.setPadding(new Insets(46, 60, 20, 60));
        heroForeground.setMaxWidth(1380);

        // Heading: "Your Next Journey \n Starts Here"
        Label heroLine1 = new Label("Your Next Journey");
        heroLine1.setFont(UiTheme.FONT_HERO_TITLE);
        heroLine1.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label heroLine2A = new Label("Starts ");
        heroLine2A.setFont(UiTheme.FONT_HERO_TITLE);
        heroLine2A.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label heroLine2B = new Label("Here");
        heroLine2B.setFont(UiTheme.FONT_HERO_TITLE);
        heroLine2B.setTextFill(UiTheme.COLOR_PRIMARY);

        HBox heroLine2 = new HBox(0, heroLine2A, heroLine2B);
        heroLine2.setAlignment(Pos.CENTER_LEFT);

        Label heroSubtitle = new Label("Rent the vehicle you need. Explore the places you love.\nSimple. Safe. Reliable.");
        heroSubtitle.setFont(UiTheme.FONT_HERO_SUBTITLE);
        heroSubtitle.setTextFill(UiTheme.COLOR_TEXT_DARK);
        heroSubtitle.setLineSpacing(4);

        VBox heroTextContainer = new VBox(6, heroLine1, heroLine2, heroSubtitle);
        heroTextContainer.setAlignment(Pos.CENTER_LEFT);

        // =====================================================================
        // SECTION C: Booking and Search Floating Panel
        // =====================================================================
        VBox bookingCard = new VBox(12);
        bookingCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        bookingCard.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        bookingCard.setEffect(UiTheme.cardShadow());
        bookingCard.setPadding(new Insets(16, 24, 20, 24));
        bookingCard.setMaxWidth(1260);

        // Tabs: Rent a Vehicle vs Find My Booking
        tabRentVehicleBtn = new Button("Rent a Vehicle");
        tabRentVehicleBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_CAR, 16, UiTheme.COLOR_WHITE));
        tabRentVehicleBtn.setFont(UiTheme.FONT_BODY_BOLD);
        tabRentVehicleBtn.setTextFill(UiTheme.COLOR_WHITE);
        tabRentVehicleBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
        tabRentVehicleBtn.setBorder(Border.EMPTY);
        tabRentVehicleBtn.setPadding(new Insets(8, 18, 8, 16));
        tabRentVehicleBtn.setCursor(Cursor.HAND);

        tabFindBookingBtn = new Button("Find My Booking");
        tabFindBookingBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_CALENDAR, 16, UiTheme.COLOR_TEXT_MUTED));
        tabFindBookingBtn.setFont(UiTheme.FONT_BODY_BOLD);
        tabFindBookingBtn.setTextFill(UiTheme.COLOR_TEXT_DARK);
        tabFindBookingBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 10));
        tabFindBookingBtn.setBorder(Border.EMPTY);
        tabFindBookingBtn.setPadding(new Insets(8, 18, 8, 16));
        tabFindBookingBtn.setCursor(Cursor.HAND);

        HBox tabHeader = new HBox(8, tabRentVehicleBtn, tabFindBookingBtn);
        tabHeader.setAlignment(Pos.CENTER_LEFT);

        // Tab 1 Content: Search Inputs
        rentTabContentBox = new VBox(8);

        // Input 1: Pickup Location
        VBox locBox = new VBox(4);
        Label locLabel = new Label("Pickup Location");
        locLabel.setFont(UiTheme.FONT_SMALL);
        locLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        locationComboBox = new ComboBox<>();
        locationComboBox.getItems().addAll(
                "Mumbai Airport (BOM) — Terminal 2",
                "Pune City Station — Central Hub",
                "Delhi IGI Airport (DEL) — Terminal 3",
                "Bengaluru Tech Park — Electronic City",
                "Goa Airport (GOI) — Coastal Hub",
                "Hyderabad — Hitec City Terminal"
        );
        locationComboBox.setPromptText("Select a location");
        locationComboBox.setPrefHeight(42);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        locationComboBox.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1));

        HBox locRow = new HBox(8, createFieldIconBadge(UiTheme.ICON_LOCATION), locationComboBox);
        HBox.setHgrow(locationComboBox, Priority.ALWAYS);
        locRow.setAlignment(Pos.CENTER_LEFT);
        locBox.getChildren().addAll(locLabel, locRow);
        HBox.setHgrow(locBox, Priority.ALWAYS);

        // Input 2: Pickup Date
        VBox pickupBox = new VBox(4);
        Label pickupLabel = new Label("Pickup Date");
        pickupLabel.setFont(UiTheme.FONT_SMALL);
        pickupLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        pickupDatePicker = createStyledDatePicker(LocalDate.now().plusDays(1));
        pickupDatePicker.setMaxWidth(Double.MAX_VALUE);
        HBox pickupRow = new HBox(8, createFieldIconBadge(UiTheme.ICON_CALENDAR), pickupDatePicker);
        HBox.setHgrow(pickupDatePicker, Priority.ALWAYS);
        pickupRow.setAlignment(Pos.CENTER_LEFT);
        pickupBox.getChildren().addAll(pickupLabel, pickupRow);
        HBox.setHgrow(pickupBox, Priority.ALWAYS);

        // Input 3: Return Date
        VBox returnBox = new VBox(4);
        Label returnLabel = new Label("Return Date");
        returnLabel.setFont(UiTheme.FONT_SMALL);
        returnLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        returnDatePicker = createStyledDatePicker(LocalDate.now().plusDays(4));
        returnDatePicker.setMaxWidth(Double.MAX_VALUE);
        HBox returnRow = new HBox(8, createFieldIconBadge(UiTheme.ICON_CALENDAR), returnDatePicker);
        HBox.setHgrow(returnDatePicker, Priority.ALWAYS);
        returnRow.setAlignment(Pos.CENTER_LEFT);
        returnBox.getChildren().addAll(returnLabel, returnRow);
        HBox.setHgrow(returnBox, Priority.ALWAYS);

        // Search Vehicles Button
        searchVehiclesBtn = new Button("Search Vehicles");
        searchVehiclesBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_SEARCH, 18, UiTheme.COLOR_WHITE));
        searchVehiclesBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        searchVehiclesBtn.setTextFill(UiTheme.COLOR_WHITE);
        searchVehiclesBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
        searchVehiclesBtn.setBorder(Border.EMPTY);
        searchVehiclesBtn.setPrefHeight(52);
        searchVehiclesBtn.setPrefWidth(200);
        searchVehiclesBtn.setCursor(Cursor.HAND);
        searchVehiclesBtn.setOnMouseEntered(e -> searchVehiclesBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_HOVER, 10)));
        searchVehiclesBtn.setOnMouseExited(e -> searchVehiclesBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10)));

        HBox rentInputsRow = new HBox(16, locBox, pickupBox, returnBox, searchVehiclesBtn);
        rentInputsRow.setAlignment(Pos.BOTTOM_CENTER);
        rentTabContentBox.getChildren().add(rentInputsRow);

        // Tab 2 Content: Find Booking
        findBookingContentBox = new VBox(10);
        findBookingContentBox.setVisible(false);
        findBookingContentBox.setManaged(false);

        Label lookupHint = new Label("Enter your booking reference ID (e.g. BKG-DEV-5001):");
        lookupHint.setFont(UiTheme.FONT_BODY);
        lookupHint.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        bookingLookupField = UiTheme.createStyledTextField("e.g. BKG-DEV-5001");
        bookingLookupField.setPrefWidth(320);

        bookingLookupBtn = UiTheme.createPrimaryButton("Lookup Booking");
        bookingLookupBtn.setPrefHeight(42);

        HBox lookupRow = new HBox(12, bookingLookupField, bookingLookupBtn);
        lookupRow.setAlignment(Pos.CENTER_LEFT);

        bookingLookupResultLabel = new Label();
        bookingLookupResultLabel.setFont(UiTheme.FONT_BODY);
        bookingLookupResultLabel.setWrapText(true);
        bookingLookupResultLabel.setVisible(false);

        bookingCancelBtn = new Button("Cancel This Reservation");
        bookingCancelBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 12));
        bookingCancelBtn.setTextFill(Color.web("#DC2626"));
        bookingCancelBtn.setBackground(UiTheme.roundedBackground(Color.web("#FEE2E2"), 8));
        bookingCancelBtn.setBorder(UiTheme.strokeBorder(Color.web("#FCA5A5"), 8, 1));
        bookingCancelBtn.setPadding(new Insets(6, 14, 6, 14));
        bookingCancelBtn.setCursor(Cursor.HAND);
        bookingCancelBtn.setVisible(false);
        bookingCancelBtn.setManaged(false);

        HBox resultRow = new HBox(12, bookingLookupResultLabel, bookingCancelBtn);
        resultRow.setAlignment(Pos.CENTER_LEFT);

        findBookingContentBox.getChildren().addAll(lookupHint, lookupRow, resultRow);

        // Validation error feedback
        bookingValidationLabel = new Label();
        bookingValidationLabel.setFont(UiTheme.FONT_BODY);
        bookingValidationLabel.setTextFill(UiTheme.COLOR_ERROR);
        bookingValidationLabel.setVisible(false);
        bookingValidationLabel.setManaged(false);

        bookingCard.getChildren().addAll(tabHeader, rentTabContentBox, findBookingContentBox, bookingValidationLabel);

        // =====================================================================
        // SECTION D: Benefits Strip (Dark Translucent Bar)
        // =====================================================================
        HBox benefitsStrip = new HBox(24);
        benefitsStrip.setAlignment(Pos.CENTER);
        benefitsStrip.setBackground(UiTheme.roundedBackground(Color.rgb(15, 23, 42, 0.78), 12));
        benefitsStrip.setBorder(UiTheme.strokeBorder(Color.rgb(255, 255, 255, 0.15), 12, 1));
        benefitsStrip.setPadding(new Insets(14, 28, 14, 28));
        benefitsStrip.setMaxWidth(1260);

        HBox benefit1 = createBenefitItem(UiTheme.ICON_SHIELD, "Trusted & Safe", "Verified vehicles");
        HBox benefit2 = createBenefitItem(UiTheme.ICON_RUPEE, "Affordable Rates", "Best prices always");
        HBox benefit3 = createBenefitItem(UiTheme.ICON_HEADSET, "24/7 Support", "We're here for you");
        HBox benefit4 = createBenefitItem(UiTheme.ICON_LOCATION, "Wide Locations", "Across major cities");

        Region bSpacer1 = new Region();
        Region bSpacer2 = new Region();
        Region bSpacer3 = new Region();
        HBox.setHgrow(bSpacer1, Priority.ALWAYS);
        HBox.setHgrow(bSpacer2, Priority.ALWAYS);
        HBox.setHgrow(bSpacer3, Priority.ALWAYS);

        benefitsStrip.getChildren().addAll(benefit1, bSpacer1, benefit2, bSpacer2, benefit3, bSpacer3, benefit4);

        heroForeground.getChildren().addAll(heroTextContainer, bookingCard, benefitsStrip);
        heroStack.getChildren().addAll(heroBg, gradientOverlay, heroForeground);
        scrollContent.getChildren().add(heroStack);

        // =====================================================================
        // SECTION E: Choose Your Ride
        // =====================================================================
        chooseRideSectionBox = new VBox(20);
        chooseRideSectionBox.setAlignment(Pos.TOP_CENTER);
        chooseRideSectionBox.setPadding(new Insets(40, 60, 44, 60));
        chooseRideSectionBox.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // Header Row: "Choose Your Ride" + "View All Vehicles →"
        HBox rideHeaderRow = new HBox();
        rideHeaderRow.setAlignment(Pos.BOTTOM_CENTER);
        rideHeaderRow.setMaxWidth(1260);

        Label rideHeading = new Label("Choose Your Ride");
        rideHeading.setFont(UiTheme.FONT_SECTION_TITLE);
        rideHeading.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label rideSubtitle = new Label("A wide range of vehicles for every journey");
        rideSubtitle.setFont(UiTheme.FONT_SECTION_SUB);
        rideSubtitle.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        VBox rideTitleBox = new VBox(4, rideHeading, rideSubtitle);
        rideTitleBox.setAlignment(Pos.CENTER_LEFT);

        Region rideSpacer = new Region();
        HBox.setHgrow(rideSpacer, Priority.ALWAYS);

        viewAllVehiclesLink = new Hyperlink("View All Vehicles →");
        viewAllVehiclesLink.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 14));
        viewAllVehiclesLink.setTextFill(UiTheme.COLOR_PRIMARY);
        viewAllVehiclesLink.setBorder(Border.EMPTY);
        viewAllVehiclesLink.setCursor(Cursor.HAND);

        rideHeaderRow.getChildren().addAll(rideTitleBox, rideSpacer, viewAllVehiclesLink);

        // Category Cards Grid
        HBox cardsGrid = new HBox(20);
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setMaxWidth(1260);

        CategoryCardComponent cardCars = new CategoryCardComponent(
                VehicleType.CAR, "Cars", "Comfort for every trip", "2,499", "5 Seats", "2 Bags", "Petrol", "car_sedan.jpg"
        );
        CategoryCardComponent cardBikes = new CategoryCardComponent(
                VehicleType.BIKE, "Bikes", "Ride the freedom", "799", "2 Seats", "45 kmpl", "Petrol", "bike_motorcycle.jpg"
        );
        CategoryCardComponent cardSuvs = new CategoryCardComponent(
                VehicleType.SUV, "SUVs", "More space, more adventure", "3,999", "7 Seats", "3 Bags", "Diesel", "suv_dark.jpg"
        );
        CategoryCardComponent cardVans = new CategoryCardComponent(
                VehicleType.VAN, "Vans", "Perfect for groups", "4,999", "12 Seats", "5 Bags", "Diesel", "van_white.jpg"
        );

        categoryCards.put(VehicleType.CAR, cardCars);
        categoryCards.put(VehicleType.BIKE, cardBikes);
        categoryCards.put(VehicleType.SUV, cardSuvs);
        categoryCards.put(VehicleType.VAN, cardVans);

        HBox.setHgrow(cardCars.getNode(), Priority.ALWAYS);
        HBox.setHgrow(cardBikes.getNode(), Priority.ALWAYS);
        HBox.setHgrow(cardSuvs.getNode(), Priority.ALWAYS);
        HBox.setHgrow(cardVans.getNode(), Priority.ALWAYS);

        cardsGrid.getChildren().addAll(cardCars.getNode(), cardBikes.getNode(), cardSuvs.getNode(), cardVans.getNode());
        chooseRideSectionBox.getChildren().addAll(rideHeaderRow, cardsGrid);
        scrollContent.getChildren().add(chooseRideSectionBox);

        // =====================================================================
        // SECTION F: How It Works
        // =====================================================================
        howItWorksSectionBox = new VBox(24);
        howItWorksSectionBox.setAlignment(Pos.TOP_CENTER);
        howItWorksSectionBox.setPadding(new Insets(44, 60, 54, 60));
        howItWorksSectionBox.setBackground(UiTheme.fillBackground(Color.web("#EFF6FF"))); // Subtle light blue

        Label howHeading = new Label("How It Works");
        howHeading.setFont(UiTheme.FONT_SECTION_TITLE);
        howHeading.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label howSub = new Label("Get on the road in just a few simple steps");
        howSub.setFont(UiTheme.FONT_SECTION_SUB);
        howSub.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        VBox howTitleBox = new VBox(4, howHeading, howSub);
        howTitleBox.setAlignment(Pos.CENTER);

        HBox stepsRow = new HBox(16);
        stepsRow.setAlignment(Pos.CENTER);
        stepsRow.setMaxWidth(1260);

        HBox step1 = createWorkflowStep(1, UiTheme.ICON_SEARCH, "Search", "Choose your location\nand travel dates");
        Label arrow1 = createStepArrow();
        HBox step2 = createWorkflowStep(2, UiTheme.ICON_CAR, "Select Vehicle", "Browse and choose\nthe perfect ride");
        Label arrow2 = createStepArrow();
        HBox step3 = createWorkflowStep(3, UiTheme.ICON_CALENDAR, "Book & Confirm", "Provide your details\nand confirm booking");
        Label arrow3 = createStepArrow();
        HBox step4 = createWorkflowStep(4, UiTheme.ICON_STEERING, "Pick Up & Ride", "Collect your vehicle\nand enjoy your journey");

        HBox.setHgrow(step1, Priority.ALWAYS);
        HBox.setHgrow(step2, Priority.ALWAYS);
        HBox.setHgrow(step3, Priority.ALWAYS);
        HBox.setHgrow(step4, Priority.ALWAYS);

        stepsRow.getChildren().addAll(step1, arrow1, step2, arrow2, step3, arrow3, step4);
        howItWorksSectionBox.getChildren().addAll(howTitleBox, stepsRow);
        scrollContent.getChildren().add(howItWorksSectionBox);

        // =====================================================================
        // SECTION G: Branded Footer
        // =====================================================================
        VBox footer = createFooter();
        scrollContent.getChildren().add(footer);

        // Main scroll wrapper
        scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8FAFC;");

        rootPane.setCenter(scrollPane);
    }

    // --- Helper Methods ---

    private Button createHeaderNavPill(String text, String svgPath, boolean isActive) {
        Button btn = new Button(text);
        if (svgPath != null) {
            Color iconColor = isActive ? UiTheme.COLOR_WHITE : UiTheme.COLOR_TEXT_DARK;
            btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, iconColor));
        }
        btn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        btn.setPadding(new Insets(7, 16, 7, 16));
        btn.setCursor(Cursor.HAND);

        if (isActive) {
            btn.setTextFill(UiTheme.COLOR_WHITE);
            btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 20));
            btn.setBorder(Border.EMPTY);
        } else {
            btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
            btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
            btn.setBorder(Border.EMPTY);

            btn.setOnMouseEntered(e -> {
                btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 20));
                btn.setTextFill(UiTheme.COLOR_PRIMARY);
            });
            btn.setOnMouseExited(e -> {
                btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
                btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
            });
        }
        return btn;
    }

    private StackPane createFieldIconBadge(String svgPath) {
        StackPane badge = new StackPane();
        badge.setPrefSize(38, 38);
        badge.setMinSize(38, 38);
        badge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 19));
        badge.getChildren().add(UiTheme.createSvgIcon(svgPath, 18, UiTheme.COLOR_PRIMARY));
        return badge;
    }

    private DatePicker createStyledDatePicker(LocalDate initialDate) {
        DatePicker dp = new DatePicker(initialDate);
        dp.setPrefHeight(42);
        dp.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        dp.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1));
        dp.setConverter(new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isBlank()) {
                    try {
                        return LocalDate.parse(string, fmt);
                    } catch (Exception ignored) {
                    }
                }
                return null;
            }
        });
        return dp;
    }

    private HBox createBenefitItem(String iconPath, String title, String subtitle) {
        StackPane circleIcon = new StackPane();
        circleIcon.setPrefSize(42, 42);
        circleIcon.setMinSize(42, 42);
        circleIcon.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 21));
        circleIcon.getChildren().add(UiTheme.createSvgIcon(iconPath, 20, UiTheme.COLOR_PRIMARY));

        Label t = new Label(title);
        t.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 14));
        t.setTextFill(UiTheme.COLOR_WHITE);

        Label s = new Label(subtitle);
        s.setFont(UiTheme.FONT_CAPTION);
        s.setTextFill(Color.web("#CBD5E1"));

        VBox text = new VBox(2, t, s);
        text.setAlignment(Pos.CENTER_LEFT);

        HBox item = new HBox(12, circleIcon, text);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private HBox createWorkflowStep(int stepNum, String iconPath, String title, String desc) {
        // Circle icon with number badge attached
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(68, 68);
        iconCircle.setMinSize(68, 68);
        iconCircle.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 34));
        iconCircle.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 34, 1.5));
        iconCircle.setEffect(UiTheme.subtleShadow());
        iconCircle.getChildren().add(UiTheme.createSvgIcon(iconPath, 28, UiTheme.COLOR_PRIMARY));

        Label numBadge = new Label(String.valueOf(stepNum));
        numBadge.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
        numBadge.setTextFill(UiTheme.COLOR_WHITE);
        numBadge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
        numBadge.setPadding(new Insets(2, 6, 2, 6));

        StackPane stepIconWithBadge = new StackPane(iconCircle, numBadge);
        StackPane.setAlignment(numBadge, Pos.TOP_LEFT);
        numBadge.setTranslateX(-4);
        numBadge.setTranslateY(-4);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        titleLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label descLabel = new Label(desc);
        descLabel.setFont(UiTheme.FONT_CAPTION);
        descLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        descLabel.setLineSpacing(2);

        VBox textBox = new VBox(2, titleLabel, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox stepBox = new HBox(12, stepIconWithBadge, textBox);
        stepBox.setAlignment(Pos.CENTER_LEFT);
        return stepBox;
    }

    private Label createStepArrow() {
        Label arrow = new Label("→");
        arrow.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 22));
        arrow.setTextFill(UiTheme.COLOR_PRIMARY);
        arrow.setPadding(new Insets(0, 4, 0, 4));
        return arrow;
    }

    private VBox createFooter() {
        VBox footer = new VBox(20);
        footer.setBackground(UiTheme.fillBackground(UiTheme.COLOR_NAVY_DARK));
        footer.setPadding(new Insets(36, 60, 24, 60));
        footer.setAlignment(Pos.CENTER);

        HBox topRow = new HBox(40);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setMaxWidth(1260);

        // Footer Brand
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

        // Contact info in footer
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

    private Image loadLocalResourceImage(String fileName) {
        try (InputStream in = getClass().getResourceAsStream("/images/" + fileName)) {
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // =========================================================================
    // Category Card Component Inner Class
    // =========================================================================
    public static class CategoryCardComponent {

        private final VehicleType vehicleType;
        private final VBox cardPane;
        private final Label priceBadgeLabel;
        private final ImageView vehicleImageView;
        private final Label categoryTitleLabel;
        private final Label subtitleLabel;
        private final Button actionArrowBtn;
        private final Label specSeatsLabel;
        private final Label specMiddleLabel;
        private final Label specFuelLabel;

        public CategoryCardComponent(VehicleType vehicleType, String categoryName, String subtitle,
                                     String initialPrice, String specSeats, String specMiddle,
                                     String specFuel, String fallbackImageResource) {
            this.vehicleType = vehicleType;

            cardPane = new VBox(10);
            cardPane.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
            cardPane.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
            cardPane.setEffect(UiTheme.subtleShadow());
            cardPane.setPadding(new Insets(16));
            cardPane.setCursor(Cursor.HAND);
            cardPane.setPrefWidth(280);
            cardPane.setMinWidth(240);

            // 1. Top Price Badge (Right aligned)
            priceBadgeLabel = new Label("From ₹ " + initialPrice + " / day");
            priceBadgeLabel.setFont(UiTheme.FONT_PRICE_BADGE);
            priceBadgeLabel.setTextFill(UiTheme.COLOR_PRIMARY);
            priceBadgeLabel.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8));
            priceBadgeLabel.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1));
            priceBadgeLabel.setPadding(new Insets(4, 10, 4, 10));

            HBox badgeRow = new HBox(priceBadgeLabel);
            badgeRow.setAlignment(Pos.CENTER_RIGHT);

            // 2. Vehicle Product Image Container
            StackPane imageContainer = new StackPane();
            imageContainer.setPrefHeight(150);
            imageContainer.setMinHeight(140);
            imageContainer.setAlignment(Pos.CENTER);

            vehicleImageView = new ImageView();
            vehicleImageView.setPreserveRatio(true);
            vehicleImageView.setFitHeight(135);
            vehicleImageView.setFitWidth(240);

            // Try fallback resource image initially
            try (InputStream in = getClass().getResourceAsStream("/images/" + fallbackImageResource)) {
                if (in != null) {
                    vehicleImageView.setImage(new Image(in));
                }
            } catch (Exception ignored) {
            }

            imageContainer.getChildren().add(vehicleImageView);

            // 3. Category Title, Subtitle, and Circle Arrow Action Button
            categoryTitleLabel = new Label(categoryName);
            categoryTitleLabel.setFont(UiTheme.FONT_CARD_TITLE);
            categoryTitleLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

            subtitleLabel = new Label(subtitle);
            subtitleLabel.setFont(UiTheme.FONT_CAPTION);
            subtitleLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

            VBox titleSubBox = new VBox(2, categoryTitleLabel, subtitleLabel);
            titleSubBox.setAlignment(Pos.CENTER_LEFT);

            actionArrowBtn = new Button("→");
            actionArrowBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
            actionArrowBtn.setTextFill(UiTheme.COLOR_PRIMARY);
            actionArrowBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 18));
            actionArrowBtn.setPrefSize(36, 36);
            actionArrowBtn.setMinSize(36, 36);
            actionArrowBtn.setCursor(Cursor.HAND);

            Region cardSpacer = new Region();
            HBox.setHgrow(cardSpacer, Priority.ALWAYS);

            HBox nameAndActionRow = new HBox(8, titleSubBox, cardSpacer, actionArrowBtn);
            nameAndActionRow.setAlignment(Pos.CENTER_LEFT);

            // 4. Divider Line
            Line cardDivider = new Line(0, 0, 240, 0);
            cardDivider.setStroke(UiTheme.COLOR_BORDER_LIGHT);

            // 5. Vehicle Specifications Row
            specSeatsLabel = new Label(specSeats);
            specSeatsLabel.setFont(UiTheme.FONT_SMALL);
            specSeatsLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            specSeatsLabel.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_SEATS, 13, UiTheme.COLOR_TEXT_MUTED));

            specMiddleLabel = new Label(specMiddle);
            specMiddleLabel.setFont(UiTheme.FONT_SMALL);
            specMiddleLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            String middleIcon = (vehicleType == VehicleType.BIKE) ? UiTheme.ICON_SPEED : UiTheme.ICON_BAGS;
            specMiddleLabel.setGraphic(UiTheme.createSvgIcon(middleIcon, 13, UiTheme.COLOR_TEXT_MUTED));

            specFuelLabel = new Label(specFuel);
            specFuelLabel.setFont(UiTheme.FONT_SMALL);
            specFuelLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            specFuelLabel.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_FUEL, 13, UiTheme.COLOR_TEXT_MUTED));

            HBox specsRow = new HBox(12, specSeatsLabel, specMiddleLabel, specFuelLabel);
            specsRow.setAlignment(Pos.CENTER_LEFT);

            cardPane.getChildren().addAll(badgeRow, imageContainer, nameAndActionRow, cardDivider, specsRow);

            // Hover animation effects
            cardPane.setOnMouseEntered(e -> {
                cardPane.setEffect(UiTheme.cardShadow());
                cardPane.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_FOCUS, 16, 1.5));
                actionArrowBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 18));
                actionArrowBtn.setTextFill(UiTheme.COLOR_WHITE);
            });
            cardPane.setOnMouseExited(e -> {
                cardPane.setEffect(UiTheme.subtleShadow());
                cardPane.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
                actionArrowBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 18));
                actionArrowBtn.setTextFill(UiTheme.COLOR_PRIMARY);
            });
        }

        public void setPrice(BigDecimal rate) {
            if (rate != null) {
                priceBadgeLabel.setText(String.format("From ₹ %,.0f / day", rate.doubleValue()));
            }
        }

        public void setImageBytes(byte[] imageBytes) {
            if (imageBytes != null && imageBytes.length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    Image img = new Image(bis, 400, 300, true, true);
                    vehicleImageView.setImage(img);
                } catch (Exception ignored) {
                }
            }
        }

        public void setSpecs(String seats, String middleSpec, String fuel) {
            if (seats != null) specSeatsLabel.setText(seats);
            if (middleSpec != null) specMiddleLabel.setText(middleSpec);
            if (fuel != null) specFuelLabel.setText(fuel);
        }

        public VBox getNode() {
            return cardPane;
        }

        public VehicleType getVehicleType() {
            return vehicleType;
        }

        public Button getActionArrowBtn() {
            return actionArrowBtn;
        }

        public void setVehicleImage(Image image) {
            if (image != null) {
                vehicleImageView.setImage(image);
            }
        }
    }

    // --- Getters for Controllers ---

    public BorderPane getRootNode() {
        return rootPane;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public HBox getBrandLogoBox() {
        return brandLogoBox;
    }

    public Button getNavHomeBtn() {
        return navHomeBtn;
    }

    public Button getNavVehiclesBtn() {
        return navVehiclesBtn;
    }

    public Button getNavHowItWorksBtn() {
        return navHowItWorksBtn;
    }

    public Button getNavAboutBtn() {
        return navAboutBtn;
    }

    public Button getNavContactBtn() {
        return navContactBtn;
    }

    public Button getNavSearchBtn() {
        return navSearchBtn;
    }

    public Button getNavLoginBtn() {
        return navLoginBtn;
    }

    public Button getNavRegisterBtn() {
        return navRegisterBtn;
    }

    public Button getTabRentVehicleBtn() {
        return tabRentVehicleBtn;
    }

    public Button getTabFindBookingBtn() {
        return tabFindBookingBtn;
    }

    public ComboBox<String> getLocationComboBox() {
        return locationComboBox;
    }

    public DatePicker getPickupDatePicker() {
        return pickupDatePicker;
    }

    public DatePicker getReturnDatePicker() {
        return returnDatePicker;
    }

    public Button getSearchVehiclesBtn() {
        return searchVehiclesBtn;
    }

    public Label getBookingValidationLabel() {
        return bookingValidationLabel;
    }

    public TextField getBookingLookupField() {
        return bookingLookupField;
    }

    public Button getBookingLookupBtn() {
        return bookingLookupBtn;
    }

    public Label getBookingLookupResultLabel() {
        return bookingLookupResultLabel;
    }

    public Button getBookingCancelBtn() {
        return bookingCancelBtn;
    }

    public VBox getRentTabContentBox() {
        return rentTabContentBox;
    }

    public VBox getFindBookingContentBox() {
        return findBookingContentBox;
    }

    public Hyperlink getViewAllVehiclesLink() {
        return viewAllVehiclesLink;
    }

    public Map<VehicleType, CategoryCardComponent> getCategoryCards() {
        return categoryCards;
    }

    public VBox getHowItWorksSectionBox() {
        return howItWorksSectionBox;
    }

    public VBox getChooseRideSectionBox() {
        return chooseRideSectionBox;
    }

    public void showValidationMessage(String message) {
        bookingValidationLabel.setText(message);
        bookingValidationLabel.setVisible(true);
        bookingValidationLabel.setManaged(true);
    }

    public void clearValidationMessage() {
        bookingValidationLabel.setText("");
        bookingValidationLabel.setVisible(false);
        bookingValidationLabel.setManaged(false);
    }
}

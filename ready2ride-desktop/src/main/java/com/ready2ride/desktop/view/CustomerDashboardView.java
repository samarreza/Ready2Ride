package com.ready2ride.desktop.view;

import com.ready2ride.core.database.DemoDataInitializer;
import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.FavoritesService;
import com.ready2ride.core.service.PaymentService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.util.UiTheme;
import com.ready2ride.desktop.util.UserSession;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Customer My Bookings page matching the reference screenshot in layout, header,
 * hero banner, customer sidebar, filter tabs, booking cards, and PostgreSQL backend integration.
 */
public class CustomerDashboardView {

    private final BorderPane rootPane;
    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final FavoritesService favoritesService;
    private final AuthenticationService authService;
    private final Runnable onNavigateToHome;
    private final Runnable onNavigateToCatalog;
    private final Runnable onLogoutToLogin;

    private final TopNavBar topNavBar;
    private final Map<String, byte[]> imageByteCache = new ConcurrentHashMap<>();
    private final Map<String, Vehicle> vehicleCache = new ConcurrentHashMap<>();

    // Sidebar navigation buttons
    private Button sidebarDashboardBtn;
    private Button sidebarMyBookingsBtn;
    private Button sidebarProfileBtn;
    private Button sidebarSavedBtn;
    private Button sidebarPaymentsBtn;
    private Button sidebarSettingsBtn;
    private Button sidebarLogoutBtn;

    // Filter Tabs
    private Button tabCurrentUpcoming;
    private Button tabPast;
    private Button tabCancelled;

    private enum TabFilter {
        ALL,
        CURRENT_UPCOMING,
        PAST,
        CANCELLED
    }

    private TabFilter activeTabFilter = TabFilter.ALL;

    // Main scroll content & sections container
    private final VBox sectionsContainer = new VBox(28);

    public CustomerDashboardView(BookingService bookingService, VehicleService vehicleService,
                                 PaymentService paymentService, FavoritesService favoritesService,
                                 AuthenticationService authService,
                                 Runnable onNavigateToHome, Runnable onNavigateToCatalog,
                                 Runnable onLogoutToLogin) {
        this.bookingService = Objects.requireNonNull(bookingService, "BookingService must not be null");
        this.vehicleService = Objects.requireNonNull(vehicleService, "VehicleService must not be null");
        this.paymentService = paymentService;
        this.favoritesService = favoritesService;
        this.authService = authService;
        this.onNavigateToHome = onNavigateToHome;
        this.onNavigateToCatalog = onNavigateToCatalog;
        this.onLogoutToLogin = onLogoutToLogin != null ? onLogoutToLogin : onNavigateToHome;

        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // 1. Shared Top Navigation Bar (Active: MY_BOOKINGS)
        topNavBar = new TopNavBar(TopNavBar.NavPage.MY_BOOKINGS);
        topNavBar.setOnNavigateHome(onNavigateToHome);
        topNavBar.setOnNavigateVehicles(onNavigateToCatalog);
        topNavBar.setOnNavigateMyBookings(this::refreshBookings);
        topNavBar.setOnNavigateAboutUs(() -> {
            if (TopNavBar.getDefaultOnNavigateAboutUs() != null) {
                TopNavBar.getDefaultOnNavigateAboutUs().run();
            } else {
                InfoDialogs.showAboutUsDialog();
            }
        });
        topNavBar.setOnNavigateContact(InfoDialogs::showContactDialog);
        topNavBar.setOnLogout(this.onLogoutToLogin);
        topNavBar.setOnLogin(this.onLogoutToLogin);

        rootPane.setTop(topNavBar);

        // 2. Main Scrollable Container (Hero Banner + Two-Column Content)
        VBox scrollContent = new VBox(0);
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // A. Hero Banner
        StackPane heroBanner = buildHeroBanner();
        scrollContent.getChildren().add(heroBanner);

        // B. Two-Column Body: Left Sidebar + Right Bookings Area
        HBox bodyLayout = new HBox(28);
        bodyLayout.setAlignment(Pos.TOP_CENTER);
        bodyLayout.setPadding(new Insets(28, 48, 40, 48));
        bodyLayout.setMaxWidth(1380);

        VBox sidebar = buildSidebar();
        VBox rightArea = buildRightBookingsArea();

        HBox.setHgrow(rightArea, Priority.ALWAYS);
        bodyLayout.getChildren().addAll(sidebar, rightArea);
        scrollContent.getChildren().add(bodyLayout);

        ScrollPane mainScroll = new ScrollPane(scrollContent);
        mainScroll.setFitToWidth(true);
        mainScroll.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        mainScroll.setBorder(Border.EMPTY);

        rootPane.setCenter(mainScroll);

        loadVehicleCache();
        refreshBookings();
    }

    public CustomerDashboardView(BookingService bookingService, VehicleService vehicleService,
                                 PaymentService paymentService, FavoritesService favoritesService,
                                 AuthenticationService authService,
                                 Runnable onNavigateToHome, Runnable onNavigateToCatalog) {
        this(bookingService, vehicleService, paymentService, favoritesService, authService, onNavigateToHome, onNavigateToCatalog, null);
    }

    public Parent getRootNode() {
        return rootPane;
    }

    // =========================================================================
    // SECTION A: Hero Banner
    // =========================================================================
    private StackPane buildHeroBanner() {
        StackPane hero = new StackPane();
        hero.setMinHeight(105);
        hero.setPrefHeight(115);
        hero.setMaxHeight(120);
        hero.setMaxWidth(Double.MAX_VALUE);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(hero.widthProperty());
        clip.heightProperty().bind(hero.heightProperty());
        hero.setClip(clip);

        // Background Image with Fallback Sunset Gradient
        ImageView heroImage = new ImageView();
        heroImage.setPreserveRatio(true);
        heroImage.fitWidthProperty().bind(hero.widthProperty());

        byte[] heroBytes = DemoDataInitializer.loadResourceImageBytes("hero_sunset.jpg");
        if (heroBytes == null) {
            heroBytes = DemoDataInitializer.loadResourceImageBytes("car_hero.jpg");
        }
        if (heroBytes != null && heroBytes.length > 0) {
            heroImage.setImage(new Image(new ByteArrayInputStream(heroBytes)));
        }

        // Dark Panoramic Gradient Overlay
        Region overlay = new Region();
        overlay.setBackground(UiTheme.fillBackground(Color.rgb(15, 23, 42, 0.65)));
        overlay.prefWidthProperty().bind(hero.widthProperty());
        overlay.prefHeightProperty().bind(hero.heightProperty());

        // Text Overlay
        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPadding(new Insets(16, 64, 16, 64));
        textBox.setMaxWidth(1380);

        HBox titleRow = new HBox(4);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label myLabel = new Label("My ");
        myLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 28));
        myLabel.setTextFill(UiTheme.COLOR_WHITE);

        Label bookingsLabel = new Label("Bookings");
        bookingsLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 28));
        bookingsLabel.setTextFill(Color.web("#38BDF8")); // Bright sky blue matching screenshot

        titleRow.getChildren().addAll(myLabel, bookingsLabel);

        Label subtitle = new Label("Manage your current, upcoming and past rentals.");
        subtitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.rgb(241, 245, 249, 0.95));

        textBox.getChildren().addAll(titleRow, subtitle);

        StackPane.setAlignment(textBox, Pos.CENTER_LEFT);
        hero.getChildren().addAll(heroImage, overlay, textBox);
        return hero;
    }

    // =========================================================================
    // SECTION B: Left Sidebar
    // =========================================================================
    private VBox buildSidebar() {
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(260);
        sidebar.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        sidebar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setEffect(UiTheme.subtleShadow());

        UserSession session = UserSession.getInstance();
        User user = session.getCurrentUser();

        // 1. Customer Profile Card
        HBox profileCard = new HBox(12);
        profileCard.setAlignment(Pos.CENTER_LEFT);
        profileCard.setPadding(new Insets(6, 6, 12, 6));

        Circle avatarCircle = new Circle(22, Color.web("#E2E8F0"));
        String initialLetter = "U";
        if (user != null && user.getFullName() != null && !user.getFullName().isBlank()) {
            initialLetter = user.getFullName().substring(0, 1).toUpperCase();
        }
        Label avatarInitial = new Label(initialLetter);
        avatarInitial.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        avatarInitial.setTextFill(Color.web("#334155"));
        StackPane avatarPane = new StackPane(avatarCircle, avatarInitial);

        VBox userTextBox = new VBox(2);
        Label nameLabel = new Label(session.getDisplayName());
        nameLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 14));
        nameLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        String email = user != null && user.getEmail() != null ? user.getEmail() : "customer@ready2ride.com";
        Label emailLabel = new Label(email);
        emailLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 11));
        emailLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        userTextBox.getChildren().addAll(nameLabel, emailLabel);
        profileCard.getChildren().addAll(avatarPane, userTextBox);

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(0, 0, 4, 0));

        // 2. Navigation Items
        sidebarDashboardBtn = createSidebarNavButton("Dashboard", UiTheme.ICON_HOME, false, onNavigateToHome);
        sidebarMyBookingsBtn = createSidebarNavButton("My Bookings", UiTheme.ICON_CALENDAR, true, this::refreshBookings);
        sidebarProfileBtn = createSidebarNavButton("Profile", UiTheme.ICON_USER, false, this::showProfileDialog);
        sidebarSavedBtn = createSidebarNavButton("Saved Vehicles", UiTheme.ICON_SHIELD, false, this::showSavedVehiclesDialog);
        sidebarPaymentsBtn = createSidebarNavButton("Payments", UiTheme.ICON_RUPEE, false, this::showPaymentsDialog);
        sidebarSettingsBtn = createSidebarNavButton("Settings", UiTheme.ICON_HOW_IT_WORKS, false, this::showSettingsDialog);

        VBox menuBox = new VBox(6,
                sidebarDashboardBtn,
                sidebarMyBookingsBtn,
                sidebarProfileBtn,
                sidebarSavedBtn,
                sidebarPaymentsBtn,
                sidebarSettingsBtn
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(4, 0, 4, 0));

        // 3. Logout Button at Bottom
        sidebarLogoutBtn = new Button("Logout");
        sidebarLogoutBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_LOGOUT, 16, UiTheme.COLOR_TEXT_MUTED));
        sidebarLogoutBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        sidebarLogoutBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        sidebarLogoutBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 8));
        sidebarLogoutBtn.setBorder(Border.EMPTY);
        sidebarLogoutBtn.setMaxWidth(Double.MAX_VALUE);
        sidebarLogoutBtn.setAlignment(Pos.CENTER_LEFT);
        sidebarLogoutBtn.setPadding(new Insets(10, 14, 10, 14));
        sidebarLogoutBtn.setCursor(Cursor.HAND);

        sidebarLogoutBtn.setOnMouseEntered(e -> {
            sidebarLogoutBtn.setBackground(UiTheme.roundedBackground(Color.web("#FEE2E2"), 8));
            sidebarLogoutBtn.setTextFill(Color.web("#EF4444"));
            sidebarLogoutBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_LOGOUT, 16, Color.web("#EF4444")));
        });
        sidebarLogoutBtn.setOnMouseExited(e -> {
            sidebarLogoutBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 8));
            sidebarLogoutBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            sidebarLogoutBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_LOGOUT, 16, UiTheme.COLOR_TEXT_MUTED));
        });

        sidebarLogoutBtn.setOnAction(e -> {
            UserSession.getInstance().logout();
            if (onLogoutToLogin != null) {
                onLogoutToLogin.run();
            }
        });

        sidebar.getChildren().addAll(profileCard, sep1, menuBox, spacer, sep2, sidebarLogoutBtn);
        return sidebar;
    }

    private Button createSidebarNavButton(String label, String svgPath, boolean isActive, Runnable action) {
        Button btn = new Button("  " + label);
        if (svgPath != null) {
            btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, isActive ? UiTheme.COLOR_PRIMARY : UiTheme.COLOR_TEXT_MUTED));
        }
        btn.setFont(Font.font(UiTheme.FONT_FAMILY, isActive ? FontWeight.BOLD : FontWeight.SEMI_BOLD, 13));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 14, 10, 14));
        btn.setCursor(Cursor.HAND);

        if (isActive) {
            btn.setTextFill(UiTheme.COLOR_PRIMARY);
            btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8));
            btn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1));
        } else {
            btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
            btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 8));
            btn.setBorder(Border.EMPTY);
            btn.setOnMouseEntered(e -> {
                btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 8));
                btn.setTextFill(UiTheme.COLOR_PRIMARY);
            });
            btn.setOnMouseExited(e -> {
                btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 8));
                btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
            });
        }

        if (action != null) {
            btn.setOnAction(e -> action.run());
        }
        return btn;
    }

    // =========================================================================
    // SECTION C: Right Bookings Area (Filter Tabs + Sections)
    // =========================================================================
    private VBox buildRightBookingsArea() {
        VBox area = new VBox(20);
        area.setAlignment(Pos.TOP_LEFT);

        // 1. Horizontal Filter Tabs Bar
        HBox tabsBar = buildFilterTabsBar();

        // 2. Sections Container
        sectionsContainer.setAlignment(Pos.TOP_LEFT);

        area.getChildren().addAll(tabsBar, sectionsContainer);
        return area;
    }

    private HBox buildFilterTabsBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        bar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        bar.setPadding(new Insets(6, 8, 6, 8));
        bar.setEffect(UiTheme.subtleShadow());

        tabCurrentUpcoming = createFilterTabButton("Current & Upcoming (0)", UiTheme.ICON_CALENDAR, true);
        tabPast = createFilterTabButton("Past Bookings (0)", UiTheme.ICON_SEARCH, false);
        tabCancelled = createFilterTabButton("Cancelled Bookings (0)", UiTheme.ICON_INFO, false);

        tabCurrentUpcoming.setOnAction(e -> {
            activeTabFilter = (activeTabFilter == TabFilter.CURRENT_UPCOMING) ? TabFilter.ALL : TabFilter.CURRENT_UPCOMING;
            updateTabStyles();
            renderSections();
        });

        tabPast.setOnAction(e -> {
            activeTabFilter = (activeTabFilter == TabFilter.PAST) ? TabFilter.ALL : TabFilter.PAST;
            updateTabStyles();
            renderSections();
        });

        tabCancelled.setOnAction(e -> {
            activeTabFilter = (activeTabFilter == TabFilter.CANCELLED) ? TabFilter.ALL : TabFilter.CANCELLED;
            updateTabStyles();
            renderSections();
        });

        bar.getChildren().addAll(tabCurrentUpcoming, tabPast, tabCancelled);
        return bar;
    }

    private Button createFilterTabButton(String text, String svgIcon, boolean active) {
        Button btn = new Button(text);
        btn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        btn.setPadding(new Insets(8, 20, 8, 20));
        btn.setCursor(Cursor.HAND);
        styleTabButton(btn, active);
        return btn;
    }

    private void styleTabButton(Button btn, boolean active) {
        if (active) {
            btn.setTextFill(UiTheme.COLOR_PRIMARY);
            btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8));
            btn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY, 8, 1.5));
        } else {
            btn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 8));
            btn.setBorder(Border.EMPTY);
            btn.setOnMouseEntered(e -> btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 8)));
            btn.setOnMouseExited(e -> btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 8)));
        }
    }

    private void updateTabStyles() {
        styleTabButton(tabCurrentUpcoming, activeTabFilter == TabFilter.CURRENT_UPCOMING || activeTabFilter == TabFilter.ALL);
        styleTabButton(tabPast, activeTabFilter == TabFilter.PAST);
        styleTabButton(tabCancelled, activeTabFilter == TabFilter.CANCELLED);
    }

    // =========================================================================
    // SECTION D: Live PostgreSQL Data Loading & Section Rendering
    // =========================================================================
    public void refreshBookings() {
        topNavBar.refreshAuthBox();
        renderSections();
    }

    private void renderSections() {
        sectionsContainer.getChildren().clear();

        String custId = UserSession.getInstance().getUserId();
        List<Booking> allBookings = bookingService.getBookingsByCustomer(custId);

        LocalDateTime now = LocalDateTime.now();

        // Categorize bookings
        List<Booking> currentAndUpcoming = new ArrayList<>();
        List<Booking> pastBookings = new ArrayList<>();
        List<Booking> cancelledBookings = new ArrayList<>();

        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.CANCELLED) {
                cancelledBookings.add(b);
            } else if (b.getStatus() == BookingStatus.COMPLETED) {
                pastBookings.add(b);
            } else if (b.getStatus() == BookingStatus.CONFIRMED) {
                if (b.getReturnDateTime().isBefore(now)) {
                    pastBookings.add(b);
                } else {
                    currentAndUpcoming.add(b);
                }
            } else {
                currentAndUpcoming.add(b);
            }
        }

        // Sort by date newest first
        currentAndUpcoming.sort(Comparator.comparing(Booking::getPickupDateTime));
        pastBookings.sort(Comparator.comparing(Booking::getPickupDateTime).reversed());
        cancelledBookings.sort(Comparator.comparing(Booking::getPickupDateTime).reversed());

        // Update Tab Labels with Live Counts
        if (tabCurrentUpcoming != null) {
            tabCurrentUpcoming.setText(String.format("📅  Current & Upcoming (%d)", currentAndUpcoming.size()));
        }
        if (tabPast != null) {
            tabPast.setText(String.format("🕒  Past Bookings (%d)", pastBookings.size()));
        }
        if (tabCancelled != null) {
            tabCancelled.setText(String.format("ⓧ  Cancelled Bookings (%d)", cancelledBookings.size()));
        }

        // If customer has no bookings at all, render global empty state
        if (allBookings.isEmpty()) {
            sectionsContainer.getChildren().add(buildEmptyStateCard(
                    "You don't have any bookings yet.",
                    "Explore our wide selection of verified cars, SUVs, bikes, and vans, and book your next journey today!"
            ));
            return;
        }

        // Render Section 1: Current & Upcoming Bookings
        if (activeTabFilter == TabFilter.ALL || activeTabFilter == TabFilter.CURRENT_UPCOMING) {
            VBox sec1 = buildSectionBlock("Current & Upcoming Bookings", "Your active and upcoming vehicle rentals.", null);
            VBox list1 = new VBox(14);
            if (currentAndUpcoming.isEmpty()) {
                list1.getChildren().add(buildEmptyStateCard("No current or upcoming rentals.", "Ready for your next trip? Browse our catalog to book a ride."));
            } else {
                for (Booking b : currentAndUpcoming) {
                    list1.getChildren().add(buildBookingCard(b));
                }
            }
            sec1.getChildren().add(list1);
            sectionsContainer.getChildren().add(sec1);
        }

        // Render Section 2: Past Bookings
        if (activeTabFilter == TabFilter.ALL || activeTabFilter == TabFilter.PAST) {
            Hyperlink viewAllLink = null;
            if (activeTabFilter == TabFilter.ALL && !pastBookings.isEmpty()) {
                viewAllLink = new Hyperlink("View All →");
                viewAllLink.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
                viewAllLink.setTextFill(UiTheme.COLOR_PRIMARY);
                viewAllLink.setOnAction(e -> {
                    activeTabFilter = TabFilter.PAST;
                    updateTabStyles();
                    renderSections();
                });
            }

            VBox sec2 = buildSectionBlock("Past Bookings", "Your completed vehicle rentals.", viewAllLink);
            VBox list2 = new VBox(14);
            if (pastBookings.isEmpty()) {
                list2.getChildren().add(buildEmptyStateCard("No past completed rentals.", "Your completed bookings will be recorded here with download receipts."));
            } else {
                for (Booking b : pastBookings) {
                    list2.getChildren().add(buildBookingCard(b));
                }
            }
            sec2.getChildren().add(list2);
            sectionsContainer.getChildren().add(sec2);
        }

        // Render Section 3: Cancelled Bookings
        if (activeTabFilter == TabFilter.ALL || activeTabFilter == TabFilter.CANCELLED) {
            Hyperlink viewAllLink = null;
            if (activeTabFilter == TabFilter.ALL && !cancelledBookings.isEmpty()) {
                viewAllLink = new Hyperlink("View All →");
                viewAllLink.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
                viewAllLink.setTextFill(UiTheme.COLOR_PRIMARY);
                viewAllLink.setOnAction(e -> {
                    activeTabFilter = TabFilter.CANCELLED;
                    updateTabStyles();
                    renderSections();
                });
            }

            VBox sec3 = buildSectionBlock("Cancelled Bookings", "Your cancelled vehicle rentals.", viewAllLink);
            VBox list3 = new VBox(14);
            if (cancelledBookings.isEmpty()) {
                list3.getChildren().add(buildEmptyStateCard("No cancelled bookings.", "Any cancelled bookings will appear here for your records."));
            } else {
                for (Booking b : cancelledBookings) {
                    list3.getChildren().add(buildBookingCard(b));
                }
            }
            sec3.getChildren().add(list3);
            sectionsContainer.getChildren().add(sec3);
        }
    }

    private VBox buildSectionBlock(String title, String subtitle, Hyperlink rightLink) {
        VBox block = new VBox(12);

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        titleLbl.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label subLbl = new Label(subtitle);
        subLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 12));
        subLbl.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        titleBox.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerRow.getChildren().addAll(titleBox, spacer);
        if (rightLink != null) {
            headerRow.getChildren().add(rightLink);
        }

        block.getChildren().add(headerRow);
        return block;
    }

    // =========================================================================
    // SECTION E: Booking Card Construction (Exact Screenshot Match)
    // =========================================================================
    private HBox buildBookingCard(Booking booking) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 14));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 14, 1));
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setEffect(UiTheme.subtleShadow());

        Vehicle v = vehicleCache.get(booking.getVehicleId());
        String brandModel = v != null ? (v.getBrand() + " " + v.getModel()) : "Vehicle #" + booking.getVehicleId();
        String typeName = v != null ? v.getVehicleType().getDisplayName() : "Vehicle";

        // 1. Vehicle Photo (Left)
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(180, 110);
        imgContainer.setMinSize(180, 110);
        imgContainer.setMaxSize(180, 110);
        imgContainer.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 8));

        ImageView iv = new ImageView();
        iv.setFitWidth(180);
        iv.setFitHeight(110);
        iv.setPreserveRatio(true);

        byte[] imgBytes = imageByteCache.get(booking.getVehicleId());
        if (imgBytes != null && imgBytes.length > 0) {
            iv.setImage(new Image(new ByteArrayInputStream(imgBytes)));
        } else {
            String fallback = getFallbackImageForVehicle(v);
            byte[] fbBytes = DemoDataInitializer.loadResourceImageBytes(fallback);
            if (fbBytes != null) {
                iv.setImage(new Image(new ByteArrayInputStream(fbBytes)));
            }
        }
        imgContainer.getChildren().add(iv);

        // 2. Vehicle Name + Category Badge + 2x2 Specs Grid
        VBox vInfoBox = new VBox(10);
        vInfoBox.setPrefWidth(220);
        vInfoBox.setMinWidth(200);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(brandModel);
        nameLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        nameLbl.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label badgeLbl = new Label(typeName);
        badgeLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 11));
        badgeLbl.setTextFill(UiTheme.COLOR_PRIMARY);
        badgeLbl.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 12));
        badgeLbl.setPadding(new Insets(2, 8, 2, 8));

        titleRow.getChildren().addAll(nameLbl, badgeLbl);

        // Specs 2x2
        HBox specsRow1 = new HBox(16);
        specsRow1.getChildren().addAll(
                createMiniSpec(UiTheme.ICON_USER, getSeatsText(v)),
                createMiniSpec(UiTheme.ICON_BAGS, getBagsText(v))
        );

        HBox specsRow2 = new HBox(16);
        specsRow2.getChildren().addAll(
                createMiniSpec(UiTheme.ICON_FUEL, getFuelText(v)),
                createMiniSpec(UiTheme.ICON_HOW_IT_WORKS, getTransmissionText(v))
        );

        vInfoBox.getChildren().addAll(titleRow, specsRow1, specsRow2);

        // 3. Status Badge & Dates Column
        VBox datesBox = new VBox(10);
        datesBox.setPrefWidth(220);
        datesBox.setMinWidth(200);

        // Determine live status badge
        HBox statusBadge = createLiveStatusBadge(booking);

        DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("hh:mm a");

        VBox pickupBox = new VBox(1);
        Label pHead = new Label("Pickup");
        pHead.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 11));
        pHead.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label pVal = new Label(booking.getPickupDateTime().format(dtfDate) + "   " + booking.getPickupDateTime().format(dtfTime));
        pVal.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 12));
        pVal.setTextFill(UiTheme.COLOR_NAVY_DARK);
        pickupBox.getChildren().addAll(pHead, pVal);

        VBox returnBox = new VBox(1);
        Label rHead = new Label("Return");
        rHead.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 11));
        rHead.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label rVal = new Label(booking.getReturnDateTime().format(dtfDate) + "   " + booking.getReturnDateTime().format(dtfTime));
        rVal.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 12));
        rVal.setTextFill(UiTheme.COLOR_NAVY_DARK);
        returnBox.getChildren().addAll(rHead, rVal);

        HBox datesGrid = new HBox(14);
        datesGrid.getChildren().addAll(
                createDateBlock("Pickup", booking.getPickupDateTime()),
                createDateBlock("Return", booking.getReturnDateTime())
        );

        datesBox.getChildren().addAll(statusBadge, datesGrid);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Price & Action Buttons (Right)
        VBox priceAndActions = new VBox(10);
        priceAndActions.setAlignment(Pos.CENTER_RIGHT);
        priceAndActions.setMinWidth(170);

        VBox priceBox = new VBox(1);
        priceBox.setAlignment(Pos.CENTER_RIGHT);

        Label totalLabel = new Label("Total Amount");
        totalLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 11));
        totalLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label amountLabel = new Label(String.format("₹ %,.0f", booking.getRentalAmount().doubleValue()));
        amountLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        amountLabel.setTextFill(UiTheme.COLOR_PRIMARY);

        priceBox.getChildren().addAll(totalLabel, amountLabel);

        // Action Buttons
        Button viewDetailsBtn = new Button("👁  View Details");
        viewDetailsBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 12));
        viewDetailsBtn.setTextFill(UiTheme.COLOR_WHITE);
        viewDetailsBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
        viewDetailsBtn.setBorder(Border.EMPTY);
        viewDetailsBtn.setPrefWidth(160);
        viewDetailsBtn.setPrefHeight(34);
        viewDetailsBtn.setCursor(Cursor.HAND);
        viewDetailsBtn.setOnAction(e -> showBookingDetailsModal(booking, v));

        priceAndActions.getChildren().addAll(priceBox, viewDetailsBtn);

        // Secondary Action Button (Cancel Booking or Download Invoice)
        if (booking.getStatus() == BookingStatus.CONFIRMED && booking.getPickupDateTime().isAfter(LocalDateTime.now())) {
            Button cancelBtn = new Button("ⓧ  Cancel Booking");
            cancelBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 12));
            cancelBtn.setTextFill(Color.web("#EF4444"));
            cancelBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            cancelBtn.setBorder(UiTheme.strokeBorder(Color.web("#EF4444"), 8, 1));
            cancelBtn.setPrefWidth(160);
            cancelBtn.setPrefHeight(34);
            cancelBtn.setCursor(Cursor.HAND);

            cancelBtn.setOnMouseEntered(e -> cancelBtn.setBackground(UiTheme.roundedBackground(Color.web("#FEE2E2"), 8)));
            cancelBtn.setOnMouseExited(e -> cancelBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8)));
            cancelBtn.setOnAction(e -> handleCancelBooking(booking));

            priceAndActions.getChildren().add(cancelBtn);
        } else if (booking.getStatus() == BookingStatus.COMPLETED || (booking.getStatus() == BookingStatus.CONFIRMED && booking.getReturnDateTime().isBefore(LocalDateTime.now()))) {
            Button invoiceBtn = new Button("⬇  Download Invoice");
            invoiceBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 12));
            invoiceBtn.setTextFill(UiTheme.COLOR_PRIMARY);
            invoiceBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            invoiceBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY, 8, 1));
            invoiceBtn.setPrefWidth(160);
            invoiceBtn.setPrefHeight(34);
            invoiceBtn.setCursor(Cursor.HAND);

            invoiceBtn.setOnMouseEntered(e -> invoiceBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8)));
            invoiceBtn.setOnMouseExited(e -> invoiceBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8)));
            invoiceBtn.setOnAction(e -> showInvoiceDialog(booking, v));

            priceAndActions.getChildren().add(invoiceBtn);
        }

        card.getChildren().addAll(imgContainer, vInfoBox, datesBox, spacer, priceAndActions);
        return card;
    }

    private HBox createDateBlock(String title, LocalDateTime ldt) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Color calColor = "Pickup".equalsIgnoreCase(title) ? Color.web("#10B981") : Color.web("#475569");
        javafx.scene.Node calIcon = UiTheme.createSvgIcon(UiTheme.ICON_CALENDAR, 18, calColor);

        VBox text = new VBox(1);
        Label head = new Label(title);
        head.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 11));
        head.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("hh:mm a");

        Label dateLbl = new Label(ldt.format(dtfDate));
        dateLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 12));
        dateLbl.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label timeLbl = new Label(ldt.format(dtfTime));
        timeLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 10));
        timeLbl.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        text.getChildren().addAll(head, dateLbl, timeLbl);
        box.getChildren().addAll(calIcon, text);
        return box;
    }

    private HBox createLiveStatusBadge(Booking b) {
        HBox badge = new HBox(6);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(3, 10, 3, 10));

        LocalDateTime now = LocalDateTime.now();

        if (b.getStatus() == BookingStatus.CANCELLED) {
            badge.setBackground(UiTheme.roundedBackground(Color.web("#FEE2E2"), 12));
            Circle dot = new Circle(3.5, Color.web("#DC2626"));
            Label lbl = new Label("Cancelled");
            lbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
            lbl.setTextFill(Color.web("#DC2626"));
            badge.getChildren().addAll(dot, lbl);
        } else if (b.getStatus() == BookingStatus.COMPLETED || b.getReturnDateTime().isBefore(now)) {
            badge.setBackground(UiTheme.roundedBackground(Color.web("#F1F5F9"), 12));
            Circle dot = new Circle(3.5, Color.web("#64748B"));
            Label lbl = new Label("Completed");
            lbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
            lbl.setTextFill(Color.web("#475569"));
            badge.getChildren().addAll(dot, lbl);
        } else if (now.isAfter(b.getPickupDateTime()) && now.isBefore(b.getReturnDateTime())) {
            badge.setBackground(UiTheme.roundedBackground(Color.web("#DCFCE7"), 12));
            Circle dot = new Circle(3.5, Color.web("#16A34A"));
            Label lbl = new Label("Ongoing");
            lbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
            lbl.setTextFill(Color.web("#15803D"));
            badge.getChildren().addAll(dot, lbl);
        } else {
            badge.setBackground(UiTheme.roundedBackground(Color.web("#DBEAFE"), 12));
            Circle dot = new Circle(3.5, Color.web("#2563EB"));
            Label lbl = new Label("Upcoming");
            lbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
            lbl.setTextFill(Color.web("#1D4ED8"));
            badge.getChildren().addAll(dot, lbl);
        }

        return badge;
    }

    private HBox createMiniSpec(String iconSvg, String label) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(
                UiTheme.createSvgIcon(iconSvg, 13, UiTheme.COLOR_TEXT_MUTED),
                new Label(label) {{
                    setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 12));
                    setTextFill(UiTheme.COLOR_TEXT_MUTED);
                }}
        );
        return box;
    }

    private VBox buildEmptyStateCard(String title, String message) {
        VBox emptyCard = new VBox(14);
        emptyCard.setAlignment(Pos.CENTER);
        emptyCard.setPadding(new Insets(48, 32, 48, 32));
        emptyCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        emptyCard.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        emptyCard.setEffect(UiTheme.subtleShadow());

        Circle iconCircle = new Circle(30, UiTheme.COLOR_PRIMARY_LIGHT);
        javafx.scene.Node icon = UiTheme.createSvgIcon(UiTheme.ICON_CAR, 28, UiTheme.COLOR_PRIMARY);
        StackPane iconPane = new StackPane(iconCircle, icon);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        titleLbl.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label msgLbl = new Label(message);
        msgLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 13));
        msgLbl.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(460);

        Button browseBtn = new Button("🚗  Browse Vehicles");
        browseBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 13));
        browseBtn.setTextFill(UiTheme.COLOR_WHITE);
        browseBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
        browseBtn.setBorder(Border.EMPTY);
        browseBtn.setPadding(new Insets(10, 24, 10, 24));
        browseBtn.setCursor(Cursor.HAND);
        browseBtn.setOnAction(e -> {
            if (onNavigateToCatalog != null) {
                onNavigateToCatalog.run();
            }
        });

        emptyCard.getChildren().addAll(iconPane, titleLbl, msgLbl, browseBtn);
        return emptyCard;
    }

    // =========================================================================
    // SECTION F: Actions (View Details, Cancel Booking, Invoice)
    // =========================================================================
    private void showBookingDetailsModal(Booking b, Vehicle v) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details — " + b.getBookingId());
        alert.setHeaderText("Reservation " + b.getBookingId() + " (" + b.getStatus() + ")");

        long days = Math.max(1, ChronoUnit.DAYS.between(b.getPickupDateTime().toLocalDate(), b.getReturnDateTime().toLocalDate()));
        double rate = v != null ? v.getRentalRate().doubleValue() : 0.0;

        alert.setContentText(String.format("""
                Vehicle: %s %s (%s)
                Registration: %s
                Customer ID: %s
                
                Pickup: %s (Main Hub)
                Return: %s (Main Hub)
                Duration: %d Day(s)
                
                Daily Rate: ₹ %,.0f / day
                Total Rental Amount: ₹ %,.0f
                Payment Status: COMPLETED
                
                Created Timestamp: %s
                """,
                v != null ? v.getBrand() : "Vehicle",
                v != null ? v.getModel() : "",
                v != null ? v.getVehicleType().getDisplayName() : "Vehicle",
                v != null ? v.getRegistrationNumber() : "MH-12-RR-0000",
                b.getCustomerId(),
                b.getPickupDateTime(),
                b.getReturnDateTime(),
                days,
                rate,
                b.getRentalAmount().doubleValue(),
                b.getCreatedAt()
        ));
        alert.showAndWait();
    }

    private void handleCancelBooking(Booking booking) {
        String custId = UserSession.getInstance().getUserId();
        Vehicle vehicle = vehicleCache.get(booking.getVehicleId());
        javafx.stage.Window window = rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;

        boolean confirmed = CancellationConfirmationDialog.show(window, booking, vehicle);
        if (confirmed) {
            try {
                // Backend ownership enforced
                bookingService.cancelBookingForCustomer(booking.getBookingId(), custId);

                CancellationConfirmationDialog.showNotification(
                        window,
                        "Booking Cancelled",
                        "Reservation Successfully Cancelled",
                        "Booking " + booking.getBookingId() + " has been updated to CANCELLED in PostgreSQL. The vehicle is now available for other customers.",
                        true
                );

                refreshBookings();
            } catch (Exception ex) {
                CancellationConfirmationDialog.showNotification(
                        window,
                        "Cancellation Error",
                        "Unable to Cancel Reservation",
                        ex.getMessage(),
                        false
                );
            }
        }
    }

    private void showInvoiceDialog(Booking b, Vehicle v) {
        String invoiceNumber = "INV-" + b.getBookingId().replace("BKG-", "");
        String formattedInvoice = String.format("""
                ============================================================
                READY2RIDE — VEHICLE RENTAL MANAGEMENT SYSTEM
                Official Rental Receipt & Booking Invoice
                ============================================================
                Invoice Number: %s
                Booking Ref:    %s
                Customer:       %s
                Date of Issue:  %s
                
                VEHICLE INFORMATION
                Model:          %s %s (%s)
                Registration:   %s
                Daily Rate:     ₹ %,.2f / day
                
                RENTAL PERIOD
                Pickup Date:    %s
                Return Date:    %s
                
                BILLING SUMMARY
                Subtotal:       ₹ %,.2f
                Taxes (18%%):   ₹ 0.00 (Included in promotional rate)
                TOTAL PAID:     ₹ %,.2f
                Payment Status: COMPLETED
                ============================================================
                Thank you for choosing Ready2Ride!
                """,
                invoiceNumber,
                b.getBookingId(),
                UserSession.getInstance().getDisplayName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                v != null ? v.getBrand() : "Vehicle",
                v != null ? v.getModel() : "",
                v != null ? v.getVehicleType().getDisplayName() : "Car",
                v != null ? v.getRegistrationNumber() : "MH-12-RR-0000",
                v != null ? v.getRentalRate().doubleValue() : 0.0,
                b.getPickupDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                b.getReturnDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                b.getRentalAmount().doubleValue(),
                b.getRentalAmount().doubleValue()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rental Invoice — " + b.getBookingId());
        alert.setHeaderText("Official Receipt: " + invoiceNumber);
        alert.setContentText(formattedInvoice);

        ButtonType saveBtn = new ButtonType("Save Invoice to File");
        alert.getButtonTypes().add(saveBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveBtn) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Invoice");
                fileChooser.setInitialFileName("Invoice_" + b.getBookingId() + ".txt");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
                File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
                if (file != null) {
                    try (PrintWriter out = new PrintWriter(file)) {
                        out.println(formattedInvoice);
                        Alert saved = new Alert(Alert.AlertType.INFORMATION);
                        saved.setTitle("Invoice Saved");
                        saved.setHeaderText("Invoice successfully exported");
                        saved.setContentText("Saved to: " + file.getAbsolutePath());
                        saved.showAndWait();
                    } catch (Exception ex) {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("Save Error");
                        err.setContentText("Could not save invoice file: " + ex.getMessage());
                        err.showAndWait();
                    }
                }
            }
        });
    }

    // =========================================================================
    // SECTION G: Customer Dialogs (Profile, Saved Vehicles, Payments, Settings)
    // =========================================================================
    private void showProfileDialog() {
        User user = UserSession.getInstance().getCurrentUser();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Profile");
        alert.setHeaderText("My Account Profile");
        alert.setContentText(String.format("""
                Name: %s
                Email: %s
                Phone: %s
                Role: %s
                Driver's License: %s
                Account Status: Active & Verified
                """,
                user != null ? user.getFullName() : "Guest",
                user != null ? user.getEmail() : "N/A",
                user != null ? user.getPhoneNumber() : "N/A",
                user != null ? user.getRole().getDisplayName() : "Customer",
                user != null && user.getDrivingLicenseNumber() != null ? user.getDrivingLicenseNumber() : "DL-2026-R2R"
        ));
        alert.showAndWait();
    }

    private void showSavedVehiclesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Saved Vehicles");
        alert.setHeaderText("Your Favorite Vehicles");
        alert.setContentText("All vehicles saved to your favorites are accessible anytime from the vehicle catalog. Click the heart icon on any vehicle card to bookmark it.");
        alert.showAndWait();
    }

    private void showPaymentsDialog() {
        String custId = UserSession.getInstance().getUserId();
        List<Booking> list = bookingService.getBookingsByCustomer(custId);
        StringBuilder sb = new StringBuilder("Transaction History:\n\n");
        double totalPaid = 0;
        for (Booking b : list) {
            if (b.getStatus() != BookingStatus.CANCELLED) {
                totalPaid += b.getRentalAmount().doubleValue();
                sb.append(String.format("• TXN-%s | %s | ₹ %,.0f | Status: COMPLETED\n",
                        b.getBookingId().replace("BKG-", ""),
                        b.getPickupDateTime().toLocalDate(),
                        b.getRentalAmount().doubleValue()));
            }
        }
        sb.append(String.format("\nTotal Lifetime Rentals Paid: ₹ %,.0f", totalPaid));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment History");
        alert.setHeaderText("Customer Transactions & Receipts");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Settings");
        alert.setHeaderText("Ready2Ride Preferences");
        alert.setContentText("Currency: INR (₹)\nTheme: Light Modern\nNotifications: Enabled (SMS & Email alerts on booking confirmations)\nTimezone: Asia/Kolkata (IST)");
        alert.showAndWait();
    }

    // =========================================================================
    // SECTION H: Helper Utilities
    // =========================================================================
    private void loadVehicleCache() {
        try {
            List<Vehicle> all = vehicleService.getAllVehicles();
            for (Vehicle v : all) {
                vehicleCache.put(v.getId(), v);
                vehicleService.getPrimaryVehicleImage(v.getId()).ifPresent(img -> {
                    if (img.getImageData() != null) {
                        imageByteCache.put(v.getId(), img.getImageData());
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    private String getSeatsText(Vehicle v) {
        if (v instanceof Car) return ((Car) v).getSeatingCapacity() + " Seats";
        if (v instanceof SUV) return ((SUV) v).getSeatingCapacity() + " Seats";
        if (v instanceof Van) return ((Van) v).getSeatingCapacity() + " Seats";
        if (v instanceof Bike) return "2 Seats";
        return "5 Seats";
    }

    private String getBagsText(Vehicle v) {
        if (v instanceof Car) return ((Car) v).getLuggageCapacityBags() + " Bags";
        if (v instanceof SUV) return ((SUV) v).getLuggageCapacityBags() + " Bags";
        if (v instanceof Van) return ((Van) v).getLuggageCapacityBags() + " Bags";
        if (v instanceof Bike) return "0 Bags";
        return "2 Bags";
    }

    private String getFuelText(Vehicle v) {
        if (v instanceof Car) return ((Car) v).getFuelType() != null ? ((Car) v).getFuelType() : "Petrol";
        if (v instanceof SUV) return ((SUV) v).getFuelType() != null ? ((SUV) v).getFuelType() : "Diesel";
        if (v instanceof Van) return ((Van) v).getFuelType() != null ? ((Van) v).getFuelType() : "Diesel";
        if (v instanceof Bike) return "Petrol";
        return "Petrol";
    }

    private String getTransmissionText(Vehicle v) {
        if (v instanceof Bike) return "Manual";
        return "Automatic";
    }

    private String getFallbackImageForVehicle(Vehicle v) {
        if (v instanceof Bike) return "bike_motorcycle.jpg";
        if (v instanceof SUV) return "suv_dark.jpg";
        if (v instanceof Van) return "van_white.jpg";
        return "car_sedan.jpg";
    }

    public TopNavBar getTopNavBar() {
        return topNavBar;
    }
}

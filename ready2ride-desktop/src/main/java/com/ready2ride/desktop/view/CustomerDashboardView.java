package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.User;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Customer Dashboard and My Bookings view matching Section 9 specifications.
 * Allows authenticated customers to view active/past/cancelled bookings,
 * cancel eligible bookings with live PostgreSQL synchronization, download invoices,
 * update profile details, view saved favorites, and review payment history.
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

    private final StackPane centerContentArea = new StackPane();
    private final Map<String, byte[]> imageByteCache = new ConcurrentHashMap<>();
    private final Map<String, Vehicle> vehicleCache = new ConcurrentHashMap<>();

    private Button navDashboardBtn;
    private Button navBookingsBtn;
    private Button navProfileBtn;
    private Button navSavedBtn;
    private Button navPaymentsBtn;
    private final List<Button> allNavButtons = new ArrayList<>();

    public CustomerDashboardView(BookingService bookingService, VehicleService vehicleService,
                                 PaymentService paymentService, FavoritesService favoritesService,
                                 AuthenticationService authService,
                                 Runnable onNavigateToHome, Runnable onNavigateToCatalog) {
        this.bookingService = Objects.requireNonNull(bookingService, "BookingService must not be null");
        this.vehicleService = Objects.requireNonNull(vehicleService, "VehicleService must not be null");
        this.paymentService = paymentService;
        this.favoritesService = favoritesService;
        this.authService = authService;
        this.onNavigateToHome = onNavigateToHome;
        this.onNavigateToCatalog = onNavigateToCatalog;

        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // 1. Top Header
        HBox topBar = buildTopBar();
        rootPane.setTop(topBar);

        // 2. Left Sidebar Navigation
        VBox sidebar = buildSidebar();
        rootPane.setLeft(sidebar);

        // 3. Center Dynamic Content
        centerContentArea.setPadding(new Insets(24, 32, 32, 32));
        centerContentArea.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        rootPane.setCenter(centerContentArea);

        loadVehicleCache();
        showMyBookingsView(); // Default to My Bookings
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.setPrefHeight(68);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 32, 10, 32));
        bar.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        bar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 0, 1));
        bar.setEffect(UiTheme.subtleShadow());

        // Brand
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setCursor(Cursor.HAND);
        brand.setOnMouseClicked(e -> {
            if (onNavigateToHome != null) onNavigateToHome.run();
        });
        brand.getChildren().addAll(
                UiTheme.createBrandLogo(34),
                new Label("Ready2Ride") {{
                    setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
                    setTextFill(UiTheme.COLOR_NAVY_DARK);
                }}
        );

        Button homeBtn = UiTheme.createGhostButton("Home");
        homeBtn.setOnAction(e -> {
            if (onNavigateToHome != null) onNavigateToHome.run();
        });

        Button vehiclesBtn = UiTheme.createGhostButton("Browse Vehicles");
        vehiclesBtn.setOnAction(e -> {
            if (onNavigateToCatalog != null) onNavigateToCatalog.run();
        });

        HBox navs = new HBox(10, homeBtn, vehiclesBtn);
        navs.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User Avatar Chip
        HBox chip = new HBox(8);
        chip.setAlignment(Pos.CENTER_LEFT);
        Circle avatar = new Circle(14, UiTheme.COLOR_PRIMARY);
        Label avatarText = new Label("U");
        avatarText.setFont(UiTheme.FONT_SMALL);
        avatarText.setTextFill(UiTheme.COLOR_WHITE);
        StackPane avPane = new StackPane(avatar, avatarText);

        Label nameLbl = new Label(UserSession.getInstance().getDisplayName());
        nameLbl.setFont(UiTheme.FONT_BODY_BOLD);
        nameLbl.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Button logoutBtn = UiTheme.createGhostButton("Logout");
        logoutBtn.setOnAction(e -> {
            UserSession.getInstance().logout();
            if (onNavigateToHome != null) onNavigateToHome.run();
        });

        chip.getChildren().addAll(avPane, nameLbl, logoutBtn);
        bar.getChildren().addAll(brand, navs, spacer, chip);
        return bar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(220);
        sidebar.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        sidebar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 0, 1));
        sidebar.setPadding(new Insets(24, 16, 24, 16));

        // Customer Profile badge
        VBox profileCard = new VBox(4);
        profileCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 12));
        profileCard.setPadding(new Insets(12, 14, 12, 14));
        Label cName = new Label(UserSession.getInstance().getDisplayName());
        cName.setFont(UiTheme.FONT_BODY_BOLD);
        cName.setTextFill(UiTheme.COLOR_PRIMARY);

        Label cRole = new Label("Customer Account");
        cRole.setFont(UiTheme.FONT_SMALL);
        cRole.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        profileCard.getChildren().addAll(cName, cRole);

        // Menu items
        navDashboardBtn = createSidebarBtn("📊  Dashboard", false, this::showDashboardOverview);
        navBookingsBtn = createSidebarBtn("📅  My Bookings", true, this::showMyBookingsView);
        navProfileBtn = createSidebarBtn("👤  Profile", false, this::showProfileView);
        navSavedBtn = createSidebarBtn("❤️  Saved Vehicles", false, this::showSavedVehiclesView);
        navPaymentsBtn = createSidebarBtn("💳  Payment History", false, this::showPaymentsView);

        allNavButtons.addAll(List.of(navDashboardBtn, navBookingsBtn, navProfileBtn, navSavedBtn, navPaymentsBtn));

        VBox menu = new VBox(6, navDashboardBtn, navBookingsBtn, navProfileBtn, navSavedBtn, navPaymentsBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.setFont(UiTheme.FONT_BODY);
        logoutBtn.setTextFill(Color.web("#EF4444"));
        logoutBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_ERROR_LIGHT, 8));
        logoutBtn.setBorder(UiTheme.strokeBorder(Color.web("#FCA5A5"), 8, 1));
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setPrefHeight(38);
        logoutBtn.setCursor(Cursor.HAND);
        logoutBtn.setOnAction(e -> {
            UserSession.getInstance().logout();
            if (onNavigateToHome != null) onNavigateToHome.run();
        });

        sidebar.getChildren().addAll(profileCard, menu, spacer, logoutBtn);
        return sidebar;
    }

    private Button createSidebarBtn(String text, boolean active, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(UiTheme.FONT_BODY);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 14, 10, 14));
        btn.setCursor(Cursor.HAND);

        if (active) {
            btn.setTextFill(UiTheme.COLOR_WHITE);
            btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
            btn.setBorder(Border.EMPTY);
        } else {
            btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
            btn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
            btn.setBorder(Border.EMPTY);
        }

        btn.setOnAction(e -> {
            for (Button b : allNavButtons) {
                b.setTextFill(UiTheme.COLOR_TEXT_DARK);
                b.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
            }
            btn.setTextFill(UiTheme.COLOR_WHITE);
            btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
            action.run();
        });

        return btn;
    }

    private void loadVehicleCache() {
        new Thread(() -> {
            try {
                List<Vehicle> list = vehicleService.getAllVehicles();
                for (Vehicle v : list) {
                    vehicleCache.put(v.getId(), v);
                    vehicleService.getPrimaryImage(v.getId()).ifPresent(img -> {
                        if (img.getImageData() != null) imageByteCache.put(v.getId(), img.getImageData());
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    // =========================================================================
    // 1. MY BOOKINGS VIEW (Tabs: All, Current & Upcoming, Past, Cancelled)
    // =========================================================================
    private void showMyBookingsView() {
        VBox view = new VBox(20);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Bookings");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button browseBtn = UiTheme.createPrimaryButton("+ Book a Vehicle");
        browseBtn.setOnAction(e -> {
            if (onNavigateToCatalog != null) onNavigateToCatalog.run();
        });

        header.getChildren().addAll(title, sp, browseBtn);

        // Filter Tabs
        HBox tabs = new HBox(10);
        tabs.setAlignment(Pos.CENTER_LEFT);

        Button tabAll = createFilterTab("All Bookings", true);
        Button tabUpcoming = createFilterTab("Current & Upcoming", false);
        Button tabPast = createFilterTab("Past Bookings", false);
        Button tabCancelled = createFilterTab("Cancelled Bookings", false);

        VBox cardsContainer = new VBox(16);

        Runnable loadBookings = () -> {
            cardsContainer.getChildren().clear();
            String custId = UserSession.getInstance().getUserId();
            List<Booking> bookings = bookingService.getBookingsByCustomer(custId);

            if (bookings.isEmpty()) {
                VBox empty = new VBox(10);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(48));
                empty.getChildren().addAll(
                        new Label("No bookings found in PostgreSQL for this account.") {{
                            setFont(UiTheme.FONT_TITLE);
                            setTextFill(UiTheme.COLOR_TEXT_MUTED);
                        }},
                        browseBtn
                );
                cardsContainer.getChildren().add(empty);
                return;
            }

            for (Booking b : bookings) {
                cardsContainer.getChildren().add(buildBookingCard(b, () -> showMyBookingsView()));
            }
        };

        tabAll.setOnAction(e -> {
            highlightTab(tabAll, tabAll, tabUpcoming, tabPast, tabCancelled);
            filterAndDisplayBookings(cardsContainer, null);
        });
        tabUpcoming.setOnAction(e -> {
            highlightTab(tabUpcoming, tabAll, tabUpcoming, tabPast, tabCancelled);
            filterAndDisplayBookings(cardsContainer, BookingStatus.CONFIRMED);
        });
        tabPast.setOnAction(e -> {
            highlightTab(tabPast, tabAll, tabUpcoming, tabPast, tabCancelled);
            filterAndDisplayBookings(cardsContainer, BookingStatus.COMPLETED);
        });
        tabCancelled.setOnAction(e -> {
            highlightTab(tabCancelled, tabAll, tabUpcoming, tabPast, tabCancelled);
            filterAndDisplayBookings(cardsContainer, BookingStatus.CANCELLED);
        });

        tabs.getChildren().addAll(tabAll, tabUpcoming, tabPast, tabCancelled);

        ScrollPane scroll = new ScrollPane(cardsContainer);
        scroll.setFitToWidth(true);
        scroll.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        scroll.setBorder(Border.EMPTY);

        view.getChildren().addAll(header, tabs, scroll);
        centerContentArea.getChildren().setAll(view);

        filterAndDisplayBookings(cardsContainer, null);
    }

    private void filterAndDisplayBookings(VBox container, BookingStatus statusFilter) {
        container.getChildren().clear();
        String custId = UserSession.getInstance().getUserId();
        List<Booking> list = bookingService.getBookingsByCustomer(custId);

        if (statusFilter != null) {
            list = list.stream().filter(b -> b.getStatus() == statusFilter).collect(Collectors.toList());
        }

        if (list.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            empty.getChildren().add(new Label("No bookings found under this filter.") {{
                setFont(UiTheme.FONT_BODY);
                setTextFill(UiTheme.COLOR_TEXT_MUTED);
            }});
            container.getChildren().add(empty);
            return;
        }

        for (Booking b : list) {
            container.getChildren().add(buildBookingCard(b, () -> filterAndDisplayBookings(container, statusFilter)));
        }
    }

    private VBox buildBookingCard(Booking booking, Runnable onRefresh) {
        VBox card = new VBox(14);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setEffect(UiTheme.subtleShadow());

        Vehicle v = vehicleCache.get(booking.getVehicleId());
        String vehicleName = v != null ? (v.getBrand() + " " + v.getModel()) : "Vehicle #" + booking.getVehicleId();
        String vehicleType = v != null ? v.getVehicleType().getDisplayName() : "Vehicle";

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label refLabel = new Label("Reference: " + booking.getBookingId());
        refLabel.setFont(UiTheme.FONT_BODY_BOLD);
        refLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label statusBadge = switch (booking.getStatus()) {
            case CONFIRMED -> UiTheme.createStatusBadge("Confirmed", UiTheme.COLOR_SUCCESS, UiTheme.COLOR_SUCCESS_LIGHT, Color.web("#A7F3D0"));
            case COMPLETED -> UiTheme.createStatusBadge("Completed", UiTheme.COLOR_PRIMARY, UiTheme.COLOR_PRIMARY_LIGHT, Color.web("#BFDBFE"));
            case CANCELLED -> UiTheme.createStatusBadge("Cancelled", UiTheme.COLOR_ERROR, UiTheme.COLOR_ERROR_LIGHT, Color.web("#FECACA"));
            default -> UiTheme.createStatusBadge(booking.getStatus().name(), UiTheme.COLOR_WARNING, UiTheme.COLOR_WARNING_LIGHT, Color.web("#FDE68A"));
        };

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label amountLabel = new Label("₹ " + String.format("%,.0f", booking.getRentalAmount().doubleValue()));
        amountLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        amountLabel.setTextFill(UiTheme.COLOR_PRIMARY);

        topRow.getChildren().addAll(refLabel, statusBadge, topSpacer, amountLabel);

        // Middle Row: Image + Details + Dates
        HBox midRow = new HBox(18);
        midRow.setAlignment(Pos.CENTER_LEFT);

        ImageView iv = new ImageView();
        iv.setPreserveRatio(true);
        iv.setFitHeight(80);
        iv.setFitWidth(120);

        byte[] imgBytes = imageByteCache.get(booking.getVehicleId());
        if (imgBytes != null && imgBytes.length > 0) {
            iv.setImage(new Image(new ByteArrayInputStream(imgBytes)));
        }

        VBox vInfo = new VBox(4);
        Label name = new Label(vehicleName);
        name.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        name.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label type = new Label(vehicleType + " • " + (v != null ? ("₹ " + v.getRentalRate() + " / day") : ""));
        type.setFont(UiTheme.FONT_CAPTION);
        type.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        vInfo.getChildren().addAll(name, type);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        VBox dateInfo = new VBox(4);
        Label pick = new Label("Pickup: " + booking.getPickupDateTime().format(dtf));
        pick.setFont(UiTheme.FONT_CAPTION);
        pick.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label ret = new Label("Return: " + booking.getReturnDateTime().format(dtf));
        ret.setFont(UiTheme.FONT_CAPTION);
        ret.setTextFill(UiTheme.COLOR_NAVY_DARK);
        dateInfo.getChildren().addAll(pick, ret);

        Region midSpacer = new Region();
        HBox.setHgrow(midSpacer, Priority.ALWAYS);

        midRow.getChildren().addAll(iv, vInfo, midSpacer, dateInfo);

        // Actions: View Details, Cancel Booking, Download Invoice
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Button viewDetailsBtn = UiTheme.createSecondaryButton("View Details");
        viewDetailsBtn.setOnAction(e -> showBookingDetailsDialog(booking, v));

        Button invoiceBtn = UiTheme.createSecondaryButton("📄  Download Invoice");
        invoiceBtn.setOnAction(e -> showInvoiceDialog(booking, v));

        Button cancelBtn = new Button("Cancel Booking");
        cancelBtn.setFont(UiTheme.FONT_BODY);
        cancelBtn.setTextFill(Color.web("#EF4444"));
        cancelBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_ERROR_LIGHT, 8));
        cancelBtn.setBorder(UiTheme.strokeBorder(Color.web("#FCA5A5"), 8, 1));
        cancelBtn.setCursor(Cursor.HAND);

        boolean canCancel = booking.getStatus() == BookingStatus.CONFIRMED && booking.getPickupDateTime().isAfter(LocalDateTime.now());
        cancelBtn.setDisable(!canCancel);

        cancelBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Reservation");
            confirm.setHeaderText("Are you sure you want to cancel booking " + booking.getBookingId() + "?");
            confirm.setContentText("The vehicle will immediately become available for other customers. Cancellation will be recorded in PostgreSQL.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        bookingService.cancelBooking(booking.getBookingId());
                        Alert ok = new Alert(Alert.AlertType.INFORMATION);
                        ok.setTitle("Booking Cancelled");
                        ok.setHeaderText("Reservation Successfully Cancelled");
                        ok.setContentText("Your booking status has been updated to CANCELLED in PostgreSQL.");
                        ok.showAndWait();
                        onRefresh.run();
                    } catch (Exception ex) {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("Cancellation Error");
                        err.setContentText(ex.getMessage());
                        err.showAndWait();
                    }
                }
            });
        });

        actionsRow.getChildren().addAll(viewDetailsBtn, invoiceBtn, cancelBtn);
        card.getChildren().addAll(topRow, midRow, actionsRow);
        return card;
    }

    private void showBookingDetailsDialog(Booking b, Vehicle v) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details — " + b.getBookingId());
        alert.setHeaderText("Reservation " + b.getBookingId() + " (" + b.getStatus() + ")");
        alert.setContentText(String.format("""
                Vehicle: %s %s
                Customer ID: %s
                Pickup Date: %s
                Return Date: %s
                Total Amount: ₹ %,.2f
                Status: %s
                Created At: %s
                """,
                v != null ? v.getBrand() : "", v != null ? v.getModel() : "",
                b.getCustomerId(),
                b.getPickupDateTime(),
                b.getReturnDateTime(),
                b.getRentalAmount().doubleValue(),
                b.getStatus(),
                b.getCreatedAt()
        ));
        alert.showAndWait();
    }

    private void showInvoiceDialog(Booking b, Vehicle v) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rental Invoice — " + b.getBookingId());
        alert.setHeaderText("Ready2Ride Official Rental Receipt");
        alert.setContentText(String.format("""
                ========================================
                READY2RIDE VEHICLE RENTAL MANAGEMENT
                Official Booking Invoice
                ========================================
                Invoice Number: INV-%s
                Booking Ref:    %s
                Customer:       %s
                
                VEHICLE DETAILS
                Model:          %s %s
                Registration:   %s
                Daily Rate:     ₹ %,.2f
                
                RENTAL PERIOD
                From:           %s
                To:             %s
                
                TOTAL CHARGES:  ₹ %,.2f
                Payment Status: COMPLETED
                ========================================
                Thank you for choosing Ready2Ride!
                """,
                b.getBookingId().replace("BKG-", ""),
                b.getBookingId(),
                UserSession.getInstance().getDisplayName(),
                v != null ? v.getBrand() : "Vehicle",
                v != null ? v.getModel() : "",
                v != null ? v.getRegistrationNumber() : "N/A",
                v != null ? v.getRentalRate().doubleValue() : 0.0,
                b.getPickupDateTime(),
                b.getReturnDateTime(),
                b.getRentalAmount().doubleValue()
        ));
        alert.showAndWait();
    }

    // =========================================================================
    // 2. DASHBOARD OVERVIEW VIEW
    // =========================================================================
    private void showDashboardOverview() {
        VBox view = new VBox(20);
        Label title = new Label("Customer Overview");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(UiTheme.COLOR_NAVY_DARK);

        String custId = UserSession.getInstance().getUserId();
        List<Booking> bookings = bookingService.getBookingsByCustomer(custId);
        long activeCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long completedCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                createStatCard("Total Reservations", String.valueOf(bookings.size()), UiTheme.COLOR_PRIMARY),
                createStatCard("Active Rides", String.valueOf(activeCount), UiTheme.COLOR_SUCCESS),
                createStatCard("Past Completed", String.valueOf(completedCount), UiTheme.COLOR_NAVY_DARK)
        );

        view.getChildren().addAll(title, stats);
        centerContentArea.getChildren().setAll(view);
    }

    // =========================================================================
    // 3. PROFILE VIEW
    // =========================================================================
    private void showProfileView() {
        VBox view = new VBox(16);
        view.setMaxWidth(480);

        Label title = new Label("Account Profile");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(UiTheme.COLOR_NAVY_DARK);

        User cur = UserSession.getInstance().getCurrentUser();
        TextField nameF = UiTheme.createStyledTextField("Full Name");
        TextField emailF = UiTheme.createStyledTextField("Email");
        TextField phoneF = UiTheme.createStyledTextField("Phone Number");
        TextField licF = UiTheme.createStyledTextField("Driving License");

        if (cur != null) {
            nameF.setText(cur.getFullName());
            emailF.setText(cur.getEmail());
            emailF.setDisable(true);
            phoneF.setText(cur.getPhoneNumber());
            if (cur.getDrivingLicenseNumber() != null) licF.setText(cur.getDrivingLicenseNumber());
        }

        Label feedback = new Label();
        feedback.setFont(UiTheme.FONT_BODY);

        Button saveBtn = UiTheme.createPrimaryButton("Save Profile Changes");
        saveBtn.setOnAction(e -> {
            if (cur != null && authService != null) {
                try {
                    authService.updateProfile(cur.getId(), nameF.getText().trim(), phoneF.getText().trim(), licF.getText().trim());
                    cur.setFullName(nameF.getText().trim());
                    cur.setPhoneNumber(phoneF.getText().trim());
                    cur.setDrivingLicenseNumber(licF.getText().trim());
                    feedback.setTextFill(UiTheme.COLOR_SUCCESS);
                    feedback.setText("Profile updated successfully in PostgreSQL!");
                } catch (Exception ex) {
                    feedback.setTextFill(UiTheme.COLOR_ERROR);
                    feedback.setText("Update failed: " + ex.getMessage());
                }
            }
        });

        view.getChildren().addAll(
                title,
                new Label("Full Name") {{ setFont(UiTheme.FONT_BODY_BOLD); }}, nameF,
                new Label("Email (Permanent)") {{ setFont(UiTheme.FONT_BODY_BOLD); }}, emailF,
                new Label("Phone Number") {{ setFont(UiTheme.FONT_BODY_BOLD); }}, phoneF,
                new Label("Driving License Number") {{ setFont(UiTheme.FONT_BODY_BOLD); }}, licF,
                feedback,
                saveBtn
        );
        centerContentArea.getChildren().setAll(view);
    }

    // =========================================================================
    // 4. SAVED VEHICLES (FAVORITES) VIEW
    // =========================================================================
    private void showSavedVehiclesView() {
        VBox view = new VBox(20);
        Label title = new Label("Saved Vehicles");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(UiTheme.COLOR_NAVY_DARK);

        FlowPane grid = new FlowPane(16, 16);
        if (favoritesService != null && UserSession.getInstance().isLoggedIn()) {
            List<Vehicle> favs = favoritesService.getFavoriteVehicles(UserSession.getInstance().getUserId());
            if (favs.isEmpty()) {
                grid.getChildren().add(new Label("No saved vehicles yet. Click the heart icon on any vehicle card in the catalog to save it.") {{
                    setFont(UiTheme.FONT_BODY);
                    setTextFill(UiTheme.COLOR_TEXT_MUTED);
                }});
            } else {
                for (Vehicle v : favs) {
                    grid.getChildren().add(buildSavedVehicleCard(v));
                }
            }
        } else {
            grid.getChildren().add(new Label("Log in to view your saved favorite vehicles.") {{
                setFont(UiTheme.FONT_BODY);
                setTextFill(UiTheme.COLOR_TEXT_MUTED);
            }});
        }

        view.getChildren().addAll(title, grid);
        centerContentArea.getChildren().setAll(view);
    }

    private VBox buildSavedVehicleCard(Vehicle v) {
        VBox c = new VBox(8);
        c.setPrefWidth(220);
        c.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        c.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        c.setPadding(new Insets(12));

        Label name = new Label(v.getBrand() + " " + v.getModel());
        name.setFont(UiTheme.FONT_BODY_BOLD);

        Label rate = new Label(String.format("₹ %,.0f / day", v.getRentalRate().doubleValue()));
        rate.setFont(UiTheme.FONT_PRICE_BADGE);
        rate.setTextFill(UiTheme.COLOR_PRIMARY);

        Button bookBtn = UiTheme.createPrimaryButton("Book Now");
        bookBtn.setOnAction(e -> {
            if (onNavigateToCatalog != null) onNavigateToCatalog.run();
        });

        c.getChildren().addAll(name, rate, bookBtn);
        return c;
    }

    // =========================================================================
    // 5. PAYMENTS HISTORY VIEW
    // =========================================================================
    private void showPaymentsView() {
        VBox view = new VBox(16);
        Label title = new Label("Payment History");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(UiTheme.COLOR_NAVY_DARK);

        VBox list = new VBox(12);
        if (paymentService != null) {
            String custId = UserSession.getInstance().getUserId();
            List<Booking> bookings = bookingService.getBookingsByCustomer(custId);

            if (bookings.isEmpty()) {
                list.getChildren().add(new Label("No payment transactions recorded.") {{
                    setFont(UiTheme.FONT_BODY);
                    setTextFill(UiTheme.COLOR_TEXT_MUTED);
                }});
            } else {
                for (Booking b : bookings) {
                    List<Payment> payments = paymentService.getPaymentsByBooking(b.getBookingId());
                    for (Payment p : payments) {
                        HBox row = new HBox(14);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
                        row.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
                        row.setPadding(new Insets(14, 20, 14, 20));

                        Label ref = new Label("Txn: " + (p.getTransactionReference() != null ? p.getTransactionReference() : p.getPaymentId()));
                        ref.setFont(UiTheme.FONT_BODY_BOLD);

                        Label m = new Label("Method: " + p.getPaymentMethod());
                        m.setFont(UiTheme.FONT_CAPTION);
                        m.setTextFill(UiTheme.COLOR_TEXT_MUTED);

                        Region sp = new Region();
                        HBox.setHgrow(sp, Priority.ALWAYS);

                        Label amt = new Label("₹ " + String.format("%,.0f", p.getAmount().doubleValue()));
                        amt.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
                        amt.setTextFill(UiTheme.COLOR_PRIMARY);

                        Label st = UiTheme.createStatusBadge(p.getStatus().name(), UiTheme.COLOR_SUCCESS, UiTheme.COLOR_SUCCESS_LIGHT, Color.web("#A7F3D0"));

                        row.getChildren().addAll(ref, m, sp, amt, st);
                        list.getChildren().add(row);
                    }
                }
            }
        }

        view.getChildren().addAll(title, list);
        centerContentArea.getChildren().setAll(view);
    }

    private Button createFilterTab(String label, boolean active) {
        Button b = new Button(label);
        b.setFont(UiTheme.FONT_BODY_BOLD);
        b.setPadding(new Insets(8, 16, 8, 16));
        b.setCursor(Cursor.HAND);
        if (active) {
            b.setTextFill(UiTheme.COLOR_WHITE);
            b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
            b.setBorder(Border.EMPTY);
        } else {
            b.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            b.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 8, 1));
        }
        return b;
    }

    private void highlightTab(Button active, Button... all) {
        for (Button b : all) {
            if (b == active) {
                b.setTextFill(UiTheme.COLOR_WHITE);
                b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
                b.setBorder(Border.EMPTY);
            } else {
                b.setTextFill(UiTheme.COLOR_TEXT_MUTED);
                b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
                b.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 8, 1));
            }
        }
    }

    private VBox createStatCard(String title, String val, Color col) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        card.setPrefWidth(200);

        Label t = new Label(title);
        t.setFont(UiTheme.FONT_CAPTION);
        t.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label v = new Label(val);
        v.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 26));
        v.setTextFill(col);

        card.getChildren().addAll(t, v);
        return card;
    }

    public BorderPane getRootNode() {
        return rootPane;
    }
}

package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.MaintenanceRecord;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.model.VehicleType;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.MaintenanceService;
import com.ready2ride.core.service.PaymentService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.util.UiTheme;
import com.ready2ride.desktop.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Rental Provider Dashboard for Ready2Ride.
 * Provides live PostgreSQL-backed fleet management and hosting controls:
 * 1. Overview (live provider fleet metrics & incoming customer bookings)
 * 2. My Vehicles (provider-owned inventory, edit rates/specs, activate/pause)
 * 3. Add Vehicle (create vehicle & upload photo to PostgreSQL BYTEA)
 * 4. Provider Bookings (customer reservations on provider's fleet)
 * 5. Profile & Settings (provider credentials & host identity)
 */
public class DashboardView {

    public record BookingTableRow(
            SimpleStringProperty id,
            SimpleStringProperty vehicle,
            SimpleStringProperty customer,
            SimpleStringProperty pickup,
            SimpleStringProperty returnTime,
            SimpleStringProperty amount,
            SimpleStringProperty status
    ) {
        public BookingTableRow(String id, String vehicle, String customer, String pickup, String returnTime, String amount, String status) {
            this(new SimpleStringProperty(id),
                 new SimpleStringProperty(vehicle),
                 new SimpleStringProperty(customer),
                 new SimpleStringProperty(pickup),
                 new SimpleStringProperty(returnTime),
                 new SimpleStringProperty(amount),
                 new SimpleStringProperty(status));
        }
    }

    private final BorderPane rootPane;
    private final StackPane contentContainer;
    private final Button navDashboardBtn;
    private final Button navVehiclesBtn;
    private final Button navAddVehicleBtn;
    private final Button navBookingsBtn;
    private final Button navProfileBtn;
    private final Button logoutBtn;
    private final List<Button> allNavButtons = new ArrayList<>();

    // Backward compatibility handles
    private final Button navCustomersBtn = new Button("Users");
    private final Button navPaymentsBtn = new Button("Payments");
    private final Button navMaintenanceBtn = new Button("Maintenance");

    private final Label providerGreetingLabel;
    private TableView<BookingTableRow> bookingsTable;

    public DashboardView() {
        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // --- 1. Left Sidebar Navigation ---
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(220);
        sidebar.setBackground(UiTheme.fillBackground(UiTheme.COLOR_NAVY_DARK));
        sidebar.setPadding(new Insets(24, 16, 24, 16));

        // Sidebar Brand Header
        StackPane logo = UiTheme.createBrandLogo(36);
        Label brandTitle = new Label("Ready2Ride");
        brandTitle.setFont(UiTheme.FONT_HEADER);
        brandTitle.setTextFill(UiTheme.COLOR_WHITE);

        Label brandBadge = new Label("RENTAL PROVIDER");
        brandBadge.setFont(UiTheme.FONT_SMALL);
        brandBadge.setTextFill(UiTheme.COLOR_PRIMARY_BORDER);

        VBox brandBox = new VBox(2, brandTitle, brandBadge);
        HBox brandHeader = new HBox(10, logo, brandBox);
        brandHeader.setAlignment(Pos.CENTER_LEFT);
        brandHeader.setPadding(new Insets(0, 0, 16, 8));

        // Navigation Menu Buttons
        navDashboardBtn = createSidebarNavButton("📊  Overview", true);
        navVehiclesBtn = createSidebarNavButton("🚗  My Vehicles", false);
        navAddVehicleBtn = createSidebarNavButton("➕  Add Vehicle", false);
        navBookingsBtn = createSidebarNavButton("📅  Provider Bookings", false);
        navProfileBtn = createSidebarNavButton("👤  Profile & Account", false);

        allNavButtons.addAll(List.of(
                navDashboardBtn, navVehiclesBtn, navAddVehicleBtn,
                navBookingsBtn, navProfileBtn
        ));

        VBox navMenu = new VBox(6,
                navDashboardBtn,
                navVehiclesBtn,
                navAddVehicleBtn,
                navBookingsBtn,
                navProfileBtn
        );

        // Sidebar Bottom / Logout
        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);

        logoutBtn = new Button("🚪  Logout");
        logoutBtn.setFont(UiTheme.FONT_BODY_BOLD);
        logoutBtn.setTextFill(Color.web("#F87171"));
        logoutBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_PANEL, 8));
        logoutBtn.setBorder(UiTheme.strokeBorder(Color.web("#7F1D1D"), 8, 1));
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setPrefHeight(38);
        logoutBtn.setCursor(Cursor.HAND);

        logoutBtn.setOnMouseEntered(e -> logoutBtn.setBackground(UiTheme.roundedBackground(Color.web("#7F1D1D"), 8)));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_PANEL, 8)));

        sidebar.getChildren().addAll(brandHeader, navMenu, sidebarSpacer, logoutBtn);
        rootPane.setLeft(sidebar);

        // --- 2. Top Header Bar ---
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 32, 16, 32));
        topBar.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        topBar.setBorder(UiTheme.bottomBorder(UiTheme.COLOR_BORDER_LIGHT, 1));

        VBox titleBox = new VBox(2);
        Label viewTitle = new Label("Rental Provider Portal");
        viewTitle.setFont(UiTheme.FONT_TITLE);
        viewTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label viewSubtitle = new Label("Manage your vehicle fleet inventory, daily rates, and customer reservations");
        viewSubtitle.setFont(UiTheme.FONT_SMALL);
        viewSubtitle.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        titleBox.getChildren().addAll(viewTitle, viewSubtitle);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        // Provider Badge & Status
        HBox adminChip = new HBox(10);
        adminChip.setAlignment(Pos.CENTER_RIGHT);

        Circle statusDot = new Circle(4, UiTheme.COLOR_SUCCESS);
        providerGreetingLabel = new Label("Host: Rental Partner");
        providerGreetingLabel.setFont(UiTheme.FONT_BODY_BOLD);
        providerGreetingLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        HBox hostBadge = new HBox(6, statusDot, providerGreetingLabel);
        hostBadge.setAlignment(Pos.CENTER);
        hostBadge.setPadding(new Insets(6, 14, 6, 14));
        hostBadge.setBackground(UiTheme.roundedBackground(Color.web("#EFF6FF"), 20));
        hostBadge.setBorder(UiTheme.roundedBorder(UiTheme.COLOR_PRIMARY_BORDER, 20, 1));

        adminChip.getChildren().add(hostBadge);
        topBar.getChildren().addAll(titleBox, topSpacer, adminChip);
        rootPane.setTop(topBar);

        // --- 3. Center Content Area ---
        contentContainer = new StackPane();
        contentContainer.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        rootPane.setCenter(contentContainer);
    }

    public void setAdminGreeting(String name) {
        if (name != null && !name.isBlank()) {
            providerGreetingLabel.setText("Host: " + name);
        }
    }

    public void updateActiveNavButton(Button activeButton) {
        for (Button b : allNavButtons) {
            if (b == activeButton) {
                b.setTextFill(UiTheme.COLOR_WHITE);
                b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
            } else {
                b.setTextFill(UiTheme.COLOR_TEXT_LIGHT);
                b.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
            }
        }
    }

    /**
     * Renders live PostgreSQL metrics on the overview dashboard for the authenticated provider.
     */
    public void renderLiveDashboardOverview(VehicleService vehicleService, BookingService bookingService,
                                            MaintenanceService maintenanceService, Runnable onRefresh) {
        VBox dashboardContent = new VBox(24);
        dashboardContent.setPadding(new Insets(28, 32, 32, 32));

        String providerId = UserSession.getInstance().getUserId();
        List<Vehicle> myVehicles = (vehicleService != null) ? vehicleService.getVehiclesByProvider(providerId) : List.of();
        if (myVehicles.isEmpty() && UserSession.getInstance().isAdmin() && vehicleService != null) {
            myVehicles = vehicleService.getAllVehicles();
        }

        int totalVehicles = myVehicles.size();
        long activeVehicles = myVehicles.stream().filter(Vehicle::isActive).count();
        long inactiveVehicles = myVehicles.stream().filter(v -> !v.isActive()).count();

        List<Booking> providerBookings = (bookingService != null) ? bookingService.getBookingsForProvider(providerId) : List.of();
        if (providerBookings.isEmpty() && UserSession.getInstance().isAdmin() && bookingService != null) {
            providerBookings = bookingService.getAllBookings();
        }

        HBox metricsRow = new HBox(18);
        metricsRow.getChildren().addAll(
                createMetricCard("Vehicles Owned", String.valueOf(totalVehicles), "Your fleet in PostgreSQL", UiTheme.COLOR_PRIMARY),
                createMetricCard("Active & Listed", String.valueOf(activeVehicles), "Rentable on customer catalog", UiTheme.COLOR_SUCCESS),
                createMetricCard("Inactive / Paused", String.valueOf(inactiveVehicles), "Temporarily offline", UiTheme.COLOR_TEXT_MUTED),
                createMetricCard("Fleet Bookings", String.valueOf(providerBookings.size()), "Total customer reservations", UiTheme.COLOR_WARNING)
        );
        HBox.setHgrow(metricsRow, Priority.ALWAYS);

        // Section: Recent Bookings
        Label tableSectionTitle = new Label("Recent Bookings for Your Vehicles (PostgreSQL)");
        tableSectionTitle.setFont(UiTheme.FONT_HEADER);
        tableSectionTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        TableView<BookingTableRow> table = createRecentBookingsTable();
        ObservableList<BookingTableRow> rows = FXCollections.observableArrayList();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Booking b : providerBookings) {
            rows.add(new BookingTableRow(
                    b.getBookingId(),
                    b.getVehicleId(),
                    b.getCustomerId(),
                    b.getPickupDateTime().format(dtf),
                    b.getReturnDateTime().format(dtf),
                    String.format("₹ %,.0f", b.getRentalAmount().doubleValue()),
                    b.getStatus().name()
            ));
        }
        table.setItems(rows);

        dashboardContent.getChildren().addAll(metricsRow, tableSectionTitle, table);

        ScrollPane scrollPane = new ScrollPane(dashboardContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        scrollPane.setBorder(Border.EMPTY);

        contentContainer.getChildren().setAll(scrollPane);
    }

    private Button createSidebarNavButton(String text, boolean active) {
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
            btn.setTextFill(UiTheme.COLOR_TEXT_LIGHT);
            btn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
            btn.setBorder(Border.EMPTY);

            btn.setOnMouseEntered(e -> btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_HOVER, 8)));
            btn.setOnMouseExited(e -> {
                if (!btn.getTextFill().equals(UiTheme.COLOR_WHITE)) {
                    btn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
                }
            });
        }
        return btn;
    }

    private VBox createMetricCard(String title, String value, String subtitle, Color accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        card.setEffect(UiTheme.subtleShadow());
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setFont(UiTheme.FONT_CAPTION);
        titleLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLabel.setTextFill(accentColor);

        Label subLabel = new Label(subtitle);
        subLabel.setFont(UiTheme.FONT_SMALL);
        subLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        card.getChildren().addAll(titleLabel, valueLabel, subLabel);
        return card;
    }

    private TableView<BookingTableRow> createRecentBookingsTable() {
        TableView<BookingTableRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(320);

        TableColumn<BookingTableRow, String> colId = new TableColumn<>("Booking Ref");
        colId.setCellValueFactory(data -> data.getValue().id());

        TableColumn<BookingTableRow, String> colVehicle = new TableColumn<>("Vehicle ID");
        colVehicle.setCellValueFactory(data -> data.getValue().vehicle());

        TableColumn<BookingTableRow, String> colCustomer = new TableColumn<>("Customer ID");
        colCustomer.setCellValueFactory(data -> data.getValue().customer());

        TableColumn<BookingTableRow, String> colPickup = new TableColumn<>("Pickup Date");
        colPickup.setCellValueFactory(data -> data.getValue().pickup());

        TableColumn<BookingTableRow, String> colReturn = new TableColumn<>("Return Date");
        colReturn.setCellValueFactory(data -> data.getValue().returnTime());

        TableColumn<BookingTableRow, String> colAmount = new TableColumn<>("Total (₹)");
        colAmount.setCellValueFactory(data -> data.getValue().amount());

        TableColumn<BookingTableRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> data.getValue().status());

        table.getColumns().addAll(List.of(colId, colVehicle, colCustomer, colPickup, colReturn, colAmount, colStatus));
        return table;
    }

    /**
     * Shows provider-owned inventory with full CRUD, rate editing, deactivation, and image upload.
     */
    public void showLiveVehiclesManagement(VehicleService vehicleService, Runnable onBack) {
        VBox mainBox = new VBox(16);
        mainBox.setPadding(new Insets(24, 32, 32, 32));

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("My Vehicle Fleet");
        title.setFont(UiTheme.FONT_TITLE);
        title.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addVehicleBtn = UiTheme.createPrimaryButton("+ Add Vehicle & Upload Image");
        addVehicleBtn.setOnAction(e -> showAddVehicleView(vehicleService, () -> showLiveVehiclesManagement(vehicleService, onBack)));

        headerRow.getChildren().addAll(backBtn, title, spacer, addVehicleBtn);

        String providerId = UserSession.getInstance().getUserId();
        List<Vehicle> list = (vehicleService != null) ? vehicleService.getVehiclesByProvider(providerId) : List.of();
        if (list.isEmpty() && UserSession.getInstance().isAdmin() && vehicleService != null) {
            list = vehicleService.getAllVehicles();
        }

        Label subLabel = new Label("Showing " + list.size() + " vehicles owned by your account (Persisted in PostgreSQL)");
        subLabel.setFont(UiTheme.FONT_CAPTION);
        subLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        if (list.isEmpty()) {
            VBox emptyBox = new VBox(16);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));
            emptyBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
            emptyBox.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));

            Label emptyTitle = new Label("No Vehicles Listed Yet");
            emptyTitle.setFont(UiTheme.FONT_HEADER);
            emptyTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

            Label emptyDesc = new Label("Start earning rental income by adding your first vehicle to the Ready2Ride fleet.");
            emptyDesc.setFont(UiTheme.FONT_BODY);
            emptyDesc.setTextFill(UiTheme.COLOR_TEXT_MUTED);

            Button emptyAddBtn = UiTheme.createPrimaryButton("+ List Your First Vehicle");
            emptyAddBtn.setOnAction(e -> showAddVehicleView(vehicleService, () -> showLiveVehiclesManagement(vehicleService, onBack)));

            emptyBox.getChildren().addAll(emptyTitle, emptyDesc, emptyAddBtn);
            mainBox.getChildren().addAll(headerRow, subLabel, emptyBox);
        } else {
            FlowPane grid = new FlowPane(16, 16);
            grid.setAlignment(Pos.TOP_LEFT);

            for (Vehicle v : list) {
                VBox card = new VBox(8);
                card.setPrefWidth(260);
                card.setPadding(new Insets(14));
                card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
                card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
                card.setEffect(UiTheme.subtleShadow());

                StackPane imgPane = new StackPane();
                imgPane.setPrefHeight(120);
                ImageView iv = new ImageView();
                iv.setPreserveRatio(true);
                iv.setFitHeight(110);
                iv.setFitWidth(230);

                Optional<VehicleImage> imgOpt = vehicleService.getPrimaryImage(v.getId());
                if (imgOpt.isPresent() && imgOpt.get().getImageData() != null) {
                    try {
                        iv.setImage(new Image(new ByteArrayInputStream(imgOpt.get().getImageData()), 230, 110, true, true));
                    } catch (Exception ignored) {
                    }
                }
                imgPane.getChildren().add(iv);

                HBox badgeRow = new HBox(8);
                badgeRow.setAlignment(Pos.CENTER_LEFT);

                Label catBadge = new Label(v.getVehicleType().name());
                catBadge.setFont(UiTheme.FONT_SMALL);
                catBadge.setTextFill(UiTheme.COLOR_PRIMARY);
                catBadge.setPadding(new Insets(2, 6, 2, 6));
                catBadge.setBackground(UiTheme.roundedBackground(Color.web("#DBEAFE"), 4));

                Label statusBadge = new Label(v.isActive() ? "● ACTIVE" : "○ INACTIVE");
                statusBadge.setFont(UiTheme.FONT_SMALL);
                statusBadge.setTextFill(v.isActive() ? UiTheme.COLOR_SUCCESS : UiTheme.COLOR_TEXT_MUTED);

                badgeRow.getChildren().addAll(catBadge, statusBadge);

                Label name = new Label(v.getBrand() + " " + v.getModel());
                name.setFont(UiTheme.FONT_BODY_BOLD);
                name.setTextFill(UiTheme.COLOR_NAVY_DARK);

                Label reg = new Label("Reg: " + v.getRegistrationNumber());
                reg.setFont(UiTheme.FONT_SMALL);
                reg.setTextFill(UiTheme.COLOR_TEXT_MUTED);

                Label rate = new Label(String.format("₹ %,.2f / day", v.getRentalRate().doubleValue()));
                rate.setFont(UiTheme.FONT_PRICE_BADGE);
                rate.setTextFill(UiTheme.COLOR_PRIMARY);

                HBox cardActions = new HBox(6);
                Button editBtn = UiTheme.createSecondaryButton("Edit Rate");
                editBtn.setOnAction(e -> openEditRateDialog(vehicleService, v, () -> showLiveVehiclesManagement(vehicleService, onBack)));

                Button uploadBtn = UiTheme.createSecondaryButton("📷 Photo");
                uploadBtn.setOnAction(e -> openImageUploadDialog(vehicleService, v, () -> showLiveVehiclesManagement(vehicleService, onBack)));

                Button toggleBtn = UiTheme.createSecondaryButton(v.isActive() ? "Pause" : "Activate");
                toggleBtn.setOnAction(e -> {
                    try {
                        v.setActive(!v.isActive());
                        vehicleService.updateProviderVehicle(v, providerId);
                        showLiveVehiclesManagement(vehicleService, onBack);
                    } catch (Exception ex) {
                        showErrorDialog("Status Update Failed", ex.getMessage());
                    }
                });

                cardActions.getChildren().addAll(editBtn, uploadBtn, toggleBtn);
                card.getChildren().addAll(imgPane, badgeRow, name, reg, rate, cardActions);
                grid.getChildren().add(card);
            }

            mainBox.getChildren().addAll(headerRow, subLabel, grid);
        }

        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);
        sp.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        sp.setBorder(Border.EMPTY);

        contentContainer.getChildren().setAll(sp);
    }

    /**
     * Dedicated Add Vehicle form view for Rental Providers.
     */
    public void showAddVehicleView(VehicleService vehicleService, Runnable onBack) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24, 32, 32, 32));
        container.setMaxWidth(680);

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = UiTheme.createSecondaryButton("← Back");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("List a New Vehicle");
        title.setFont(UiTheme.FONT_TITLE);
        title.setTextFill(UiTheme.COLOR_TEXT_DARK);
        headerRow.getChildren().addAll(backBtn, title);

        Label subLabel = new Label("Add your vehicle specifications and upload a photo to persist directly to PostgreSQL.");
        subLabel.setFont(UiTheme.FONT_BODY);
        subLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(24));
        formCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        formCard.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        formCard.setEffect(UiTheme.subtleShadow());

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(VehicleType.CAR);
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        TextField brandField = UiTheme.createStyledTextField("Brand (e.g. Toyota, Hyundai, Honda)");
        TextField modelField = UiTheme.createStyledTextField("Model (e.g. Corolla, Creta, City)");
        TextField regField = UiTheme.createStyledTextField("Registration Number (e.g. MH-12-AB-1234)");
        TextField rateField = UiTheme.createStyledTextField("Daily Rental Rate in ₹ (e.g. 2499)");
        TextField seatsField = UiTheme.createStyledTextField("Seating Capacity (e.g. 5)");
        TextField fuelField = UiTheme.createStyledTextField("Fuel Type (e.g. Petrol, Diesel, Electric)");

        final byte[][] uploadedBytes = new byte[1][];
        final String[] uploadedFileName = new String[]{"vehicle.jpg"};

        Label imgHeader = new Label("Vehicle Image (Persisted to PostgreSQL BYTEA):");
        imgHeader.setFont(UiTheme.FONT_BODY_BOLD);

        TextField filePathField = UiTheme.createStyledTextField("No file selected yet");
        filePathField.setEditable(false);

        Button browseBtn = UiTheme.createSecondaryButton("Browse Photo...");
        ImageView preview = new ImageView();
        preview.setFitWidth(240);
        preview.setFitHeight(130);
        preview.setPreserveRatio(true);

        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Vehicle Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
            );
            File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
            if (file != null && file.exists()) {
                try {
                    uploadedBytes[0] = Files.readAllBytes(file.toPath());
                    uploadedFileName[0] = file.getName();
                    filePathField.setText(file.getName() + " (" + uploadedBytes[0].length + " bytes)");
                    preview.setImage(new Image(new ByteArrayInputStream(uploadedBytes[0])));
                } catch (Exception ex) {
                    filePathField.setText("Error reading file: " + ex.getMessage());
                }
            }
        });

        HBox fileRow = new HBox(10, filePathField, browseBtn);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(filePathField, Priority.ALWAYS);

        Label errorLabel = new Label();
        errorLabel.setFont(UiTheme.FONT_BODY);
        errorLabel.setTextFill(UiTheme.COLOR_ERROR);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(12, 0, 0, 0));

        Button cancelBtn = UiTheme.createSecondaryButton("Cancel");
        cancelBtn.setOnAction(e -> onBack.run());

        Button saveBtn = UiTheme.createPrimaryButton("Save Vehicle & Image");
        saveBtn.setPrefHeight(40);

        saveBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            String brand = brandField.getText() != null ? brandField.getText().trim() : "";
            String model = modelField.getText() != null ? modelField.getText().trim() : "";
            String reg = regField.getText() != null ? regField.getText().trim() : "";
            String rateStr = rateField.getText() != null ? rateField.getText().trim() : "";
            String fuel = fuelField.getText() != null ? fuelField.getText().trim() : "Petrol";
            String seatsStr = seatsField.getText() != null ? seatsField.getText().trim() : "5";

            if (brand.isEmpty() || model.isEmpty() || reg.isEmpty() || rateStr.isEmpty()) {
                errorLabel.setText("Please fill in Brand, Model, Registration, and Daily Rate.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            BigDecimal rate;
            try {
                rate = new BigDecimal(rateStr);
                if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                errorLabel.setText("Please enter a valid positive daily rental rate in ₹.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            int seats = 5;
            try {
                seats = Integer.parseInt(seatsStr);
            } catch (Exception ignored) {
            }

            String providerId = UserSession.getInstance().getUserId();
            String vehicleId = "VEH-PROV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            VehicleType type = typeCombo.getValue();

            Vehicle newVehicle = switch (type) {
                case CAR -> new Car(vehicleId, brand, model, reg, rate, seats, fuel, 2);
                case BIKE -> new Bike(vehicleId, brand, model, reg, rate, 150, 45);
                case SUV -> new SUV(vehicleId, brand, model, reg, rate, false, seats, 3);
                case VAN -> new Van(vehicleId, brand, model, reg, rate, 1000.0, seats, 5);
            };

            try {
                vehicleService.registerProviderVehicle(newVehicle, providerId);

                if (uploadedBytes[0] != null) {
                    VehicleImage img = new VehicleImage(
                            "IMG-" + vehicleId,
                            vehicleId,
                            uploadedFileName[0],
                            "image/jpeg",
                            uploadedBytes[0],
                            true
                    );
                    vehicleService.saveProviderVehicleImage(img, providerId);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Vehicle Added");
                alert.setHeaderText("Vehicle Successfully Persisted in PostgreSQL");
                alert.setContentText("Your " + brand + " " + model + " is now listed and available for customer reservations.");
                alert.showAndWait();
                onBack.run();
            } catch (Exception ex) {
                errorLabel.setText("Failed to save vehicle: " + ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });

        btnRow.getChildren().addAll(cancelBtn, saveBtn);

        formCard.getChildren().addAll(
                new Label("Vehicle Category *"), typeCombo,
                new Label("Brand *"), brandField,
                new Label("Model *"), modelField,
                new Label("Registration Number *"), regField,
                new Label("Daily Rental Rate (₹) *"), rateField,
                new Label("Seating Capacity"), seatsField,
                new Label("Fuel Type"), fuelField,
                imgHeader, fileRow, preview,
                errorLabel,
                btnRow
        );

        container.getChildren().addAll(headerRow, subLabel, formCard);

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        sp.setBorder(Border.EMPTY);

        contentContainer.getChildren().setAll(sp);
    }

    /**
     * Provider Bookings review table showing customer reservations on provider's vehicles.
     */
    public void showLiveBookingsManagement(BookingService bookingService, VehicleService vehicleService, Runnable onBack) {
        VBox mainBox = new VBox(16);
        mainBox.setPadding(new Insets(24, 32, 32, 32));

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Provider Bookings");
        title.setFont(UiTheme.FONT_TITLE);
        title.setTextFill(UiTheme.COLOR_TEXT_DARK);

        headerRow.getChildren().addAll(backBtn, title);

        String providerId = UserSession.getInstance().getUserId();
        List<Booking> bookings = (bookingService != null) ? bookingService.getBookingsForProvider(providerId) : List.of();
        if (bookings.isEmpty() && UserSession.getInstance().isAdmin() && bookingService != null) {
            bookings = bookingService.getAllBookings();
        }

        Label subLabel = new Label("Showing " + bookings.size() + " customer reservations on your fleet vehicles.");
        subLabel.setFont(UiTheme.FONT_CAPTION);
        subLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        TableView<BookingTableRow> table = createRecentBookingsTable();
        table.setPrefHeight(480);
        ObservableList<BookingTableRow> rows = FXCollections.observableArrayList();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Booking b : bookings) {
            rows.add(new BookingTableRow(
                    b.getBookingId(),
                    b.getVehicleId(),
                    b.getCustomerId(),
                    b.getPickupDateTime().format(dtf),
                    b.getReturnDateTime().format(dtf),
                    String.format("₹ %,.0f", b.getRentalAmount().doubleValue()),
                    b.getStatus().name()
            ));
        }
        table.setItems(rows);

        mainBox.getChildren().addAll(headerRow, subLabel, table);

        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);
        sp.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        sp.setBorder(Border.EMPTY);

        contentContainer.getChildren().setAll(sp);
    }

    /**
     * Provider Profile and Account Settings.
     */
    public void showProfileManagement(AuthenticationService authService, Runnable onBack) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24, 32, 32, 32));
        container.setMaxWidth(600);

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = UiTheme.createSecondaryButton("← Back");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Rental Provider Profile");
        title.setFont(UiTheme.FONT_TITLE);
        title.setTextFill(UiTheme.COLOR_TEXT_DARK);
        headerRow.getChildren().addAll(backBtn, title);

        User currentUser = UserSession.getInstance().getCurrentUser();

        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));

        TextField nameField = UiTheme.createStyledTextField(currentUser != null ? currentUser.getFullName() : "");
        TextField emailField = UiTheme.createStyledTextField(currentUser != null ? currentUser.getEmail() : "");
        emailField.setEditable(false);

        TextField phoneField = UiTheme.createStyledTextField(currentUser != null ? currentUser.getPhoneNumber() : "");
        TextField licenseField = UiTheme.createStyledTextField(currentUser != null && currentUser.getDrivingLicenseNumber() != null ? currentUser.getDrivingLicenseNumber() : "");

        Label roleBadge = new Label("Role: " + (currentUser != null ? currentUser.getRole().getDisplayName() : "Rental Provider"));
        roleBadge.setFont(UiTheme.FONT_BODY_BOLD);
        roleBadge.setTextFill(UiTheme.COLOR_PRIMARY);

        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        Button saveBtn = UiTheme.createPrimaryButton("Save Profile Changes");
        saveBtn.setOnAction(e -> {
            String newName = nameField.getText() != null ? nameField.getText().trim() : "";
            String newPhone = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String newLic = licenseField.getText() != null ? licenseField.getText().trim() : "";

            if (newName.isEmpty() || newPhone.isEmpty()) {
                feedbackLabel.setText("Name and phone number cannot be empty.");
                feedbackLabel.setTextFill(UiTheme.COLOR_ERROR);
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }

            try {
                if (authService != null && currentUser != null) {
                    authService.updateProfile(currentUser.getId(), newName, newPhone, newLic);
                    currentUser.setFullName(newName);
                    currentUser.setPhoneNumber(newPhone);
                    currentUser.setDrivingLicenseNumber(newLic);
                    setAdminGreeting(newName);
                }
                feedbackLabel.setText("Profile updated successfully in PostgreSQL!");
                feedbackLabel.setTextFill(UiTheme.COLOR_SUCCESS);
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            } catch (Exception ex) {
                feedbackLabel.setText("Update failed: " + ex.getMessage());
                feedbackLabel.setTextFill(UiTheme.COLOR_ERROR);
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            }
        });

        card.getChildren().addAll(
                roleBadge,
                new Label("Full Name *"), nameField,
                new Label("Email Address (Login ID)"), emailField,
                new Label("Phone Number *"), phoneField,
                new Label("Host ID / Driving License"), licenseField,
                feedbackLabel,
                saveBtn
        );

        container.getChildren().addAll(headerRow, card);

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        sp.setBorder(Border.EMPTY);

        contentContainer.getChildren().setAll(sp);
    }

    private void openEditRateDialog(VehicleService vehicleService, Vehicle v, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Rental Rate — " + v.getBrand() + " " + v.getModel());
        dialog.setHeaderText("Update Daily Rental Rate in PostgreSQL");

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));
        TextField rateField = UiTheme.createStyledTextField(v.getRentalRate().toString());
        rateField.setText(v.getRentalRate().toString());

        Label err = new Label();
        err.setTextFill(UiTheme.COLOR_ERROR);

        form.getChildren().addAll(new Label("Daily Rental Rate (₹):"), rateField, err);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            try {
                BigDecimal newRate = new BigDecimal(rateField.getText().trim());
                v.setRentalRate(newRate);
                String providerId = UserSession.getInstance().getUserId();
                vehicleService.updateProviderVehicle(v, providerId);
                onRefresh.run();
            } catch (Exception ex) {
                err.setText("Invalid rate: " + ex.getMessage());
                ev.consume();
            }
        });
        dialog.showAndWait();
    }

    private void openImageUploadDialog(VehicleService vehicleService, Vehicle v, Runnable onRefresh) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Vehicle Photo to Persist in PostgreSQL");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null && file.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                VehicleImage img = new VehicleImage("IMG-" + v.getId(), v.getId(), file.getName(), "image/jpeg", bytes, true);
                String providerId = UserSession.getInstance().getUserId();
                vehicleService.saveProviderVehicleImage(img, providerId);

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Image Saved");
                a.setHeaderText("Vehicle Photo Uploaded Successfully");
                a.setContentText("Persisted " + bytes.length + " bytes to PostgreSQL BYTEA column for " + v.getBrand() + " " + v.getModel());
                a.showAndWait();
                onRefresh.run();
            } catch (Exception ex) {
                showErrorDialog("Upload Failed", ex.getMessage());
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        err.setTitle(title);
        err.setHeaderText("Operation Error");
        err.setContentText(message);
        err.showAndWait();
    }

    // Getters for controller wiring
    public Parent getRootNode() {
        return rootPane;
    }

    public Button getNavDashboardBtn() {
        return navDashboardBtn;
    }

    public Button getNavVehiclesBtn() {
        return navVehiclesBtn;
    }

    public Button getNavAddVehicleBtn() {
        return navAddVehicleBtn;
    }

    public Button getNavBookingsBtn() {
        return navBookingsBtn;
    }

    public Button getNavProfileBtn() {
        return navProfileBtn;
    }

    public Button getLogoutBtn() {
        return logoutBtn;
    }

    public Button getNavCustomersBtn() {
        return navCustomersBtn;
    }

    public Button getNavPaymentsBtn() {
        return navPaymentsBtn;
    }

    public Button getNavMaintenanceBtn() {
        return navMaintenanceBtn;
    }

    public void restoreDashboardContent() {
    }
}

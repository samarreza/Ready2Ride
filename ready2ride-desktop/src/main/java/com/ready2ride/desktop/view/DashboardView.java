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
import javafx.scene.control.DatePicker;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fully functional Administrator Console and Management View for Ready2Ride.
 * Provides real PostgreSQL-backed administration for:
 * 1. Overview Dashboard (live fleet & reservation metrics)
 * 2. Fleet Vehicles Management (full CRUD + binary BYTEA image upload via FileChooser)
 * 3. Bookings Management (reservation review & status updates)
 * 4. User Directory (customer & admin account status management)
 * 5. Payment Transaction Ledger (financial audit & payment methods)
 * 6. Maintenance Management (service logging & booking conflict prevention)
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
    private final Button navBookingsBtn;
    private final Button navCustomersBtn;
    private final Button navPaymentsBtn;
    private final Button navMaintenanceBtn;
    private final Button logoutBtn;
    private final List<Button> allNavButtons = new ArrayList<>();

    private final Label adminGreetingLabel;
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

        Label brandBadge = new Label("ADMIN CONSOLE");
        brandBadge.setFont(UiTheme.FONT_SMALL);
        brandBadge.setTextFill(UiTheme.COLOR_PRIMARY_BORDER);

        VBox brandBox = new VBox(2, brandTitle, brandBadge);
        HBox brandHeader = new HBox(10, logo, brandBox);
        brandHeader.setAlignment(Pos.CENTER_LEFT);
        brandHeader.setPadding(new Insets(0, 0, 16, 8));

        // Navigation Menu Buttons
        navDashboardBtn = createSidebarNavButton("📊  Dashboard", true);
        navVehiclesBtn = createSidebarNavButton("🚗  Vehicles", false);
        navBookingsBtn = createSidebarNavButton("📅  Bookings", false);
        navCustomersBtn = createSidebarNavButton("👥  Users & Customers", false);
        navPaymentsBtn = createSidebarNavButton("💳  Payments", false);
        navMaintenanceBtn = createSidebarNavButton("🔧  Maintenance", false);

        allNavButtons.addAll(List.of(
                navDashboardBtn, navVehiclesBtn, navBookingsBtn,
                navCustomersBtn, navPaymentsBtn, navMaintenanceBtn
        ));

        VBox navMenu = new VBox(6,
                navDashboardBtn,
                navVehiclesBtn,
                navBookingsBtn,
                navCustomersBtn,
                navPaymentsBtn,
                navMaintenanceBtn
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
        topBar.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        topBar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 0, 1));
        topBar.setPadding(new Insets(16, 32, 16, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);

        adminGreetingLabel = new Label("Dashboard Overview");
        adminGreetingLabel.setFont(UiTheme.FONT_HEADER);
        adminGreetingLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label subHeaderLabel = new Label("•  Fleet Management Console");
        subHeaderLabel.setFont(UiTheme.FONT_BODY);
        subHeaderLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        HBox titleBox = new HBox(8, adminGreetingLabel, subHeaderLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        // Administrator Profile Chip
        Circle avatarCircle = new Circle(16, UiTheme.COLOR_PRIMARY);
        Label avatarText = new Label("AD");
        avatarText.setFont(UiTheme.FONT_SMALL);
        avatarText.setTextFill(UiTheme.COLOR_WHITE);
        StackPane avatarPane = new StackPane(avatarCircle, avatarText);

        Label profileName = new Label("Fleet Administrator");
        profileName.setFont(UiTheme.FONT_BODY_BOLD);
        profileName.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Circle statusDot = new Circle(4, UiTheme.COLOR_SUCCESS);
        Label statusText = new Label("PostgreSQL Connected");
        statusText.setFont(UiTheme.FONT_SMALL);
        statusText.setTextFill(UiTheme.COLOR_SUCCESS);
        HBox statusRow = new HBox(4, statusDot, statusText);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        VBox profileMeta = new VBox(2, profileName, statusRow);
        HBox profileChip = new HBox(10, avatarPane, profileMeta);
        profileChip.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 20));
        profileChip.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 20, 1));
        profileChip.setPadding(new Insets(6, 14, 6, 8));
        profileChip.setAlignment(Pos.CENTER_LEFT);

        topBar.getChildren().addAll(titleBox, topSpacer, profileChip);
        rootPane.setTop(topBar);

        // --- 3. Main Center Content Container ---
        contentContainer = new StackPane();
        contentContainer.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        rootPane.setCenter(contentContainer);

        // Render Initial Overview
        renderInitialOverview();
    }

    private void renderInitialOverview() {
        VBox dashboardContent = new VBox(24);
        dashboardContent.setPadding(new Insets(28, 32, 32, 32));

        // Metric Cards (Placeholder counts before live injection)
        HBox metricsRow = new HBox(18);
        metricsRow.getChildren().addAll(
                createMetricCard("Total Vehicles", "8", "4 Categories registered", UiTheme.COLOR_PRIMARY),
                createMetricCard("Available Vehicles", "7", "Ready for immediate rental", UiTheme.COLOR_SUCCESS),
                createMetricCard("Active Bookings", "2", "Current reservations", UiTheme.COLOR_WARNING),
                createMetricCard("Pending Maintenance", "1", "Scheduled inspections", UiTheme.COLOR_ERROR)
        );
        HBox.setHgrow(metricsRow, Priority.ALWAYS);

        // Section: Recent Bookings
        Label tableSectionTitle = new Label("Recent Bookings (PostgreSQL)");
        tableSectionTitle.setFont(UiTheme.FONT_HEADER);
        tableSectionTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        bookingsTable = createRecentBookingsTable();

        dashboardContent.getChildren().addAll(metricsRow, tableSectionTitle, bookingsTable);

        ScrollPane scrollPane = new ScrollPane(dashboardContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        scrollPane.setBorder(Border.EMPTY);

        contentContainer.getChildren().setAll(scrollPane);
    }

    /**
     * Renders live PostgreSQL metrics on the overview dashboard.
     */
    public void renderLiveDashboardOverview(VehicleService vehicleService, BookingService bookingService,
                                            MaintenanceService maintenanceService, Runnable onRefresh) {
        VBox dashboardContent = new VBox(24);
        dashboardContent.setPadding(new Insets(28, 32, 32, 32));

        int totalVehicles = vehicleService != null ? vehicleService.getAllVehicles().size() : 0;
        long activeVehicles = vehicleService != null ? vehicleService.getAllVehicles().stream().filter(Vehicle::isActive).count() : 0;
        List<Booking> allBookings = bookingService != null ? bookingService.getAllBookings() : List.of();
        long activeBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long maintenanceCount = maintenanceService != null ? maintenanceService.getAllMaintenance().stream().filter(m -> !m.isCompleted()).count() : 0;

        HBox metricsRow = new HBox(18);
        metricsRow.getChildren().addAll(
                createMetricCard("Total Vehicles", String.valueOf(totalVehicles), "Fleet size in PostgreSQL", UiTheme.COLOR_PRIMARY),
                createMetricCard("Available Vehicles", String.valueOf(activeVehicles), "Active and rentable", UiTheme.COLOR_SUCCESS),
                createMetricCard("Active Bookings", String.valueOf(activeBookings), "Current customer reservations", UiTheme.COLOR_WARNING),
                createMetricCard("Pending Maintenance", String.valueOf(maintenanceCount), "Scheduled service records", UiTheme.COLOR_ERROR)
        );
        HBox.setHgrow(metricsRow, Priority.ALWAYS);

        Label tableSectionTitle = new Label("Recent Bookings (PostgreSQL)");
        tableSectionTitle.setFont(UiTheme.FONT_HEADER);
        tableSectionTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        TableView<BookingTableRow> table = createRecentBookingsTable();
        ObservableList<BookingTableRow> rows = FXCollections.observableArrayList();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Booking b : allBookings) {
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
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        card.setEffect(UiTheme.subtleShadow());

        Label titleLabel = new Label(title);
        titleLabel.setFont(UiTheme.FONT_BODY);
        titleLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 28));
        valueLabel.setTextFill(accentColor);

        Label subLabel = new Label(subtitle);
        subLabel.setFont(UiTheme.FONT_SMALL);
        subLabel.setTextFill(UiTheme.COLOR_TEXT_HINT);

        card.getChildren().addAll(titleLabel, valueLabel, subLabel);
        return card;
    }

    private TableView<BookingTableRow> createRecentBookingsTable() {
        TableView<BookingTableRow> table = new TableView<>();
        table.setPrefHeight(320);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        table.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        table.setEffect(UiTheme.subtleShadow());

        TableColumn<BookingTableRow, String> colId = new TableColumn<>("Booking ID");
        colId.setCellValueFactory(cellData -> cellData.getValue().id());
        colId.setPrefWidth(120);

        TableColumn<BookingTableRow, String> colVehicle = new TableColumn<>("Vehicle ID");
        colVehicle.setCellValueFactory(cellData -> cellData.getValue().vehicle());
        colVehicle.setPrefWidth(140);

        TableColumn<BookingTableRow, String> colCustomer = new TableColumn<>("Customer ID");
        colCustomer.setCellValueFactory(cellData -> cellData.getValue().customer());
        colCustomer.setPrefWidth(130);

        TableColumn<BookingTableRow, String> colPickup = new TableColumn<>("Pickup Date");
        colPickup.setCellValueFactory(cellData -> cellData.getValue().pickup());
        colPickup.setPrefWidth(140);

        TableColumn<BookingTableRow, String> colReturn = new TableColumn<>("Return Date");
        colReturn.setCellValueFactory(cellData -> cellData.getValue().returnTime());
        colReturn.setPrefWidth(140);

        TableColumn<BookingTableRow, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amount());
        colAmount.setPrefWidth(90);

        TableColumn<BookingTableRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().status());
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colId, colVehicle, colCustomer, colPickup, colReturn, colAmount, colStatus);
        return table;
    }

    // =========================================================================
    // 2. LIVE VEHICLES MANAGEMENT (Full CRUD + FileChooser Image Upload)
    // =========================================================================
    public void showLiveVehiclesManagement(VehicleService vehicleService, Runnable onBack) {
        VBox mainBox = new VBox(16);
        mainBox.setPadding(new Insets(24, 32, 32, 32));

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Fleet Vehicles Management");
        title.setFont(UiTheme.FONT_TITLE);
        title.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addVehicleBtn = UiTheme.createPrimaryButton("+ Add Vehicle & Upload Image");
        addVehicleBtn.setOnAction(e -> openAddVehicleDialog(vehicleService, () -> showLiveVehiclesManagement(vehicleService, onBack)));

        headerRow.getChildren().addAll(backBtn, title, spacer, addVehicleBtn);

        Label subLabel = new Label("Live PostgreSQL Fleet Records & Binary BYTEA Vehicle Images");
        subLabel.setFont(UiTheme.FONT_CAPTION);
        subLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        FlowPane grid = new FlowPane(16, 16);
        grid.setAlignment(Pos.TOP_LEFT);

        List<Vehicle> list = vehicleService.getAllVehicles();
        for (Vehicle v : list) {
            VBox card = new VBox(8);
            card.setPrefWidth(250);
            card.setPadding(new Insets(14));
            card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
            card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
            card.setEffect(UiTheme.subtleShadow());

            StackPane imgPane = new StackPane();
            imgPane.setPrefHeight(120);
            ImageView iv = new ImageView();
            iv.setPreserveRatio(true);
            iv.setFitHeight(110);
            iv.setFitWidth(220);

            Optional<VehicleImage> imgOpt = vehicleService.getPrimaryImage(v.getId());
            if (imgOpt.isPresent() && imgOpt.get().getImageData() != null) {
                try {
                    iv.setImage(new Image(new ByteArrayInputStream(imgOpt.get().getImageData()), 220, 110, true, true));
                } catch (Exception ignored) {
                }
            }
            imgPane.getChildren().add(iv);

            Label name = new Label(v.getBrand() + " " + v.getModel());
            name.setFont(UiTheme.FONT_BODY_BOLD);
            name.setTextFill(UiTheme.COLOR_NAVY_DARK);

            Label reg = new Label("Reg: " + v.getRegistrationNumber() + " • " + v.getVehicleType());
            reg.setFont(UiTheme.FONT_SMALL);
            reg.setTextFill(UiTheme.COLOR_TEXT_MUTED);

            Label rate = new Label(String.format("₹ %,.2f / day", v.getRentalRate().doubleValue()));
            rate.setFont(UiTheme.FONT_PRICE_BADGE);
            rate.setTextFill(UiTheme.COLOR_PRIMARY);

            HBox cardActions = new HBox(8);
            Button editBtn = UiTheme.createSecondaryButton("Edit Rate");
            editBtn.setOnAction(e -> openEditRateDialog(vehicleService, v, () -> showLiveVehiclesManagement(vehicleService, onBack)));

            Button uploadBtn = UiTheme.createSecondaryButton("📷 Upload");
            uploadBtn.setOnAction(e -> openImageUploadDialog(vehicleService, v, () -> showLiveVehiclesManagement(vehicleService, onBack)));

            cardActions.getChildren().addAll(editBtn, uploadBtn);

            card.getChildren().addAll(imgPane, name, reg, rate, cardActions);
            grid.getChildren().add(card);
        }

        VBox contentBox = new VBox(16, headerRow, subLabel, grid);
        ScrollPane sp = new ScrollPane(contentBox);
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
                vehicleService.registerVehicle(v);
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
        chooser.setTitle("Select Vehicle Image to Persist in PostgreSQL");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null && file.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                VehicleImage img = new VehicleImage("IMG-" + v.getId(), v.getId(), file.getName(), "image/jpeg", bytes, true);
                vehicleService.saveVehicleImage(img);

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Image Saved");
                a.setHeaderText("Vehicle Image Uploaded Successfully");
                a.setContentText("Persisted " + bytes.length + " bytes to PostgreSQL BYTEA column for " + v.getBrand() + " " + v.getModel());
                a.showAndWait();
                onRefresh.run();
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Upload Failed");
                err.setContentText(ex.getMessage());
                err.showAndWait();
            }
        }
    }

    private void openAddVehicleDialog(VehicleService vehicleService, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add New Fleet Vehicle");
        dialog.setHeaderText("Create Vehicle & Upload Image into PostgreSQL\nAll specifications and image bytes will persist in PostgreSQL.");

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));
        form.setPrefWidth(420);

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(VehicleType.CAR);

        TextField brandField = UiTheme.createStyledTextField("Brand (e.g. Hyundai)");
        TextField modelField = UiTheme.createStyledTextField("Model (e.g. Creta SX)");
        TextField regField = UiTheme.createStyledTextField("Reg Number (e.g. MH-12-AB-9988)");
        TextField rateField = UiTheme.createStyledTextField("Daily Rate (e.g. 2999.00)");
        TextField seatsField = UiTheme.createStyledTextField("Seats (e.g. 5)");
        TextField fuelField = UiTheme.createStyledTextField("Fuel Type (e.g. Petrol)");

        Label imgLabel = new Label("Vehicle Image File (JPEG/PNG):");
        imgLabel.setFont(UiTheme.FONT_SMALL);
        imgLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        TextField filePathField = UiTheme.createStyledTextField("Select or enter image file path...");
        filePathField.setEditable(false);

        Button browseBtn = new Button("Browse...");
        browseBtn.setFont(UiTheme.FONT_BODY);

        final byte[][] uploadedBytes = new byte[1][];
        final String[] uploadedFileName = new String[] { "vehicle.jpg" };

        ImageView preview = new ImageView();
        preview.setFitWidth(180);
        preview.setFitHeight(100);
        preview.setPreserveRatio(true);

        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Vehicle Image File");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
            );
            File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
            if (file != null && file.exists()) {
                try {
                    uploadedBytes[0] = Files.readAllBytes(file.toPath());
                    uploadedFileName[0] = file.getName();
                    filePathField.setText(file.getAbsolutePath());
                    preview.setImage(new Image(new ByteArrayInputStream(uploadedBytes[0])));
                } catch (Exception ex) {
                    filePathField.setText("Error reading file: " + ex.getMessage());
                }
            }
        });

        HBox fileRow = new HBox(8, filePathField, browseBtn);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(filePathField, Priority.ALWAYS);

        Label errLabel = new Label();
        errLabel.setFont(UiTheme.FONT_SMALL);
        errLabel.setTextFill(UiTheme.COLOR_ERROR);

        form.getChildren().addAll(
                new Label("Vehicle Category:"), typeCombo,
                new Label("Brand:"), brandField,
                new Label("Model:"), modelField,
                new Label("Registration Number:"), regField,
                new Label("Daily Rental Rate (₹):"), rateField,
                new Label("Seating Capacity:"), seatsField,
                new Label("Fuel Type:"), fuelField,
                imgLabel, fileRow, preview, errLabel
        );

        dialog.getDialogPane().setContent(new ScrollPane(form));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Save Vehicle & Image");

        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String brand = brandField.getText() != null ? brandField.getText().trim() : "";
            String model = modelField.getText() != null ? modelField.getText().trim() : "";
            String reg = regField.getText() != null ? regField.getText().trim() : "";
            String rateStr = rateField.getText() != null ? rateField.getText().trim() : "";

            if (brand.isEmpty() || model.isEmpty() || reg.isEmpty() || rateStr.isEmpty()) {
                errLabel.setText("Please fill all required vehicle fields.");
                event.consume();
                return;
            }

            BigDecimal rate;
            try {
                rate = new BigDecimal(rateStr);
            } catch (Exception ex) {
                errLabel.setText("Invalid rental rate number.");
                event.consume();
                return;
            }

            int seats = 5;
            try {
                seats = Integer.parseInt(seatsField.getText().trim());
            } catch (Exception ignored) {
            }

            String fuel = fuelField.getText() != null && !fuelField.getText().isBlank() ? fuelField.getText().trim() : "Petrol";

            try {
                String newVehicleId = typeCombo.getValue().name() + "-ADM-" + System.currentTimeMillis();
                Vehicle v = switch (typeCombo.getValue()) {
                    case CAR -> new Car(newVehicleId, brand, model, reg, rate, seats, fuel, 2);
                    case BIKE -> new Bike(newVehicleId, brand, model, reg, rate, 350, 40);
                    case SUV -> new SUV(newVehicleId, brand, model, reg, rate, true, seats, 3, fuel);
                    case VAN -> new Van(newVehicleId, brand, model, reg, rate, 1200.0, seats, 4, fuel);
                };

                vehicleService.registerVehicle(v);

                byte[] data = uploadedBytes[0];
                if (data == null || data.length == 0) {
                    data = com.ready2ride.core.database.DemoDataInitializer.loadResourceImageBytes(
                            switch (typeCombo.getValue()) {
                                case CAR -> "car_sedan.jpg";
                                case BIKE -> "bike_motorcycle.jpg";
                                case SUV -> "suv_dark.jpg";
                                case VAN -> "van_white.jpg";
                            }
                    );
                }

                if (data != null && data.length > 0) {
                    String imgId = "IMG-" + newVehicleId;
                    VehicleImage vi = new VehicleImage(imgId, newVehicleId, uploadedFileName[0], "image/jpeg", data, true);
                    vehicleService.saveVehicleImage(vi);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Vehicle Added");
                alert.setHeaderText("Vehicle & Image Successfully Persisted in PostgreSQL!");
                alert.setContentText("Registered " + brand + " " + model + " (" + reg + ") with persistent image bytes.");
                alert.showAndWait();

                onRefresh.run();
            } catch (Exception ex) {
                errLabel.setText("Failed to save: " + ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    // =========================================================================
    // 3. LIVE BOOKINGS MANAGEMENT
    // =========================================================================
    public void showLiveBookingsManagement(BookingService bookingService, VehicleService vehicleService, Runnable onBack) {
        VBox view = new VBox(16);
        view.setPadding(new Insets(24, 32, 32, 32));

        HBox head = new HBox(16);
        head.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());
        Label title = new Label("All Fleet Bookings (PostgreSQL)");
        title.setFont(UiTheme.FONT_TITLE);
        head.getChildren().addAll(backBtn, title);

        TableView<BookingTableRow> table = createRecentBookingsTable();
        table.setPrefHeight(480);

        List<Booking> list = bookingService.getAllBookings();
        ObservableList<BookingTableRow> rows = FXCollections.observableArrayList();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Booking b : list) {
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

        HBox actions = new HBox(12);
        Button cancelBtn = UiTheme.createSecondaryButton("Cancel Selected Booking");
        cancelBtn.setOnAction(e -> {
            BookingTableRow selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                bookingService.cancelBooking(selected.id().get());
                showLiveBookingsManagement(bookingService, vehicleService, onBack);
            }
        });
        actions.getChildren().add(cancelBtn);

        view.getChildren().addAll(head, table, actions);
        contentContainer.getChildren().setAll(view);
    }

    // =========================================================================
    // 4. LIVE USERS MANAGEMENT
    // =========================================================================
    public void showLiveUsersManagement(AuthenticationService authService, Runnable onBack) {
        VBox view = new VBox(16);
        view.setPadding(new Insets(24, 32, 32, 32));

        HBox head = new HBox(16);
        head.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());
        Label title = new Label("User & Customer Directory (PostgreSQL)");
        title.setFont(UiTheme.FONT_TITLE);
        head.getChildren().addAll(backBtn, title);

        TableView<User> table = new TableView<>();
        table.setPrefHeight(480);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhoneNumber()));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().name()));

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "ACTIVE" : "DEACTIVATED"));

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, statusCol);

        if (authService != null) {
            table.setItems(FXCollections.observableArrayList(authService.getAllUsers()));
        }

        HBox actions = new HBox(12);
        Button toggleBtn = UiTheme.createSecondaryButton("Toggle Active/Deactivated");
        toggleBtn.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && authService != null) {
                authService.toggleUserActive(sel.getId(), !sel.isActive());
                showLiveUsersManagement(authService, onBack);
            }
        });
        actions.getChildren().add(toggleBtn);

        view.getChildren().addAll(head, table, actions);
        contentContainer.getChildren().setAll(view);
    }

    // =========================================================================
    // 5. LIVE PAYMENTS MANAGEMENT
    // =========================================================================
    public void showLivePaymentsManagement(PaymentService paymentService, Runnable onBack) {
        VBox view = new VBox(16);
        view.setPadding(new Insets(24, 32, 32, 32));

        HBox head = new HBox(16);
        head.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());
        Label title = new Label("Payment Transaction Ledger (PostgreSQL)");
        title.setFont(UiTheme.FONT_TITLE);
        head.getChildren().addAll(backBtn, title);

        TableView<Payment> table = new TableView<>();
        table.setPrefHeight(480);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Payment, String> idCol = new TableColumn<>("Transaction Ref");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionReference() != null ? c.getValue().getTransactionReference() : c.getValue().getPaymentId()));

        TableColumn<Payment, String> bkgCol = new TableColumn<>("Booking ID");
        bkgCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingId()));

        TableColumn<Payment, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹ %,.2f", c.getValue().getAmount().doubleValue())));

        TableColumn<Payment, String> methodCol = new TableColumn<>("Payment Method");
        methodCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod().name()));

        TableColumn<Payment, String> stCol = new TableColumn<>("Status");
        stCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        TableColumn<Payment, String> dateCol = new TableColumn<>("Timestamp");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentDateTime().format(dtf)));

        table.getColumns().addAll(idCol, bkgCol, amtCol, methodCol, stCol, dateCol);

        if (paymentService != null) {
            table.setItems(FXCollections.observableArrayList(paymentService.getAllPayments()));
        }

        view.getChildren().addAll(head, table);
        contentContainer.getChildren().setAll(view);
    }

    // =========================================================================
    // 6. LIVE MAINTENANCE MANAGEMENT
    // =========================================================================
    public void showLiveMaintenanceManagement(MaintenanceService maintenanceService, VehicleService vehicleService, Runnable onBack) {
        VBox view = new VBox(16);
        view.setPadding(new Insets(24, 32, 32, 32));

        HBox head = new HBox(16);
        head.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = UiTheme.createSecondaryButton("← Back to Overview");
        backBtn.setOnAction(e -> onBack.run());
        Label title = new Label("Fleet Maintenance & Service (PostgreSQL)");
        title.setFont(UiTheme.FONT_TITLE);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button scheduleBtn = UiTheme.createPrimaryButton("+ Schedule Maintenance");
        scheduleBtn.setOnAction(e -> openScheduleMaintenanceDialog(maintenanceService, vehicleService, () -> showLiveMaintenanceManagement(maintenanceService, vehicleService, onBack)));

        head.getChildren().addAll(backBtn, title, sp, scheduleBtn);

        TableView<MaintenanceRecord> table = new TableView<>();
        table.setPrefHeight(480);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MaintenanceRecord, String> idCol = new TableColumn<>("Record ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecordId()));

        TableColumn<MaintenanceRecord, String> vCol = new TableColumn<>("Vehicle ID");
        vCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVehicleId()));

        TableColumn<MaintenanceRecord, String> dateCol = new TableColumn<>("Service Date");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getServiceDate().toString()));

        TableColumn<MaintenanceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        TableColumn<MaintenanceRecord, String> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹ %,.2f", c.getValue().getCost().doubleValue())));

        TableColumn<MaintenanceRecord, String> stCol = new TableColumn<>("Status");
        stCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isCompleted() ? "COMPLETED" : "SCHEDULED"));

        table.getColumns().addAll(idCol, vCol, dateCol, descCol, costCol, stCol);

        if (maintenanceService != null) {
            table.setItems(FXCollections.observableArrayList(maintenanceService.getAllMaintenance()));
        }

        HBox actions = new HBox(12);
        Button completeBtn = UiTheme.createSecondaryButton("Mark Selected Completed");
        completeBtn.setOnAction(e -> {
            MaintenanceRecord sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && maintenanceService != null) {
                maintenanceService.updateCompletionStatus(sel.getRecordId(), true);
                showLiveMaintenanceManagement(maintenanceService, vehicleService, onBack);
            }
        });
        actions.getChildren().add(completeBtn);

        view.getChildren().addAll(head, table, actions);
        contentContainer.getChildren().setAll(view);
    }

    private void openScheduleMaintenanceDialog(MaintenanceService maintenanceService, VehicleService vehicleService, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Schedule Vehicle Maintenance");
        dialog.setHeaderText("Create Maintenance Entry in PostgreSQL");

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        ComboBox<String> vCombo = new ComboBox<>();
        if (vehicleService != null) {
            for (Vehicle v : vehicleService.getAllVehicles()) {
                vCombo.getItems().add(v.getId() + " - " + v.getBrand() + " " + v.getModel());
            }
            if (!vCombo.getItems().isEmpty()) vCombo.setValue(vCombo.getItems().get(0));
        }

        DatePicker dp = new DatePicker(LocalDate.now().plusDays(2));
        TextField descField = UiTheme.createStyledTextField("Brake inspection & oil change");
        TextField costField = UiTheme.createStyledTextField("2500.00");

        form.getChildren().addAll(
                new Label("Vehicle:"), vCombo,
                new Label("Service Date:"), dp,
                new Label("Description:"), descField,
                new Label("Estimated Cost (₹):"), costField
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            try {
                String vSel = vCombo.getValue();
                String vId = vSel.split(" - ")[0];
                maintenanceService.scheduleMaintenance(vId, dp.getValue(), descField.getText().trim(), new BigDecimal(costField.getText().trim()));
                onRefresh.run();
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setContentText(ex.getMessage());
                err.showAndWait();
                ev.consume();
            }
        });

        dialog.showAndWait();
    }

    public void updateActiveNavButton(Button activeButton) {
        for (Button btn : allNavButtons) {
            if (btn == activeButton) {
                btn.setTextFill(UiTheme.COLOR_WHITE);
                btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
            } else {
                btn.setTextFill(UiTheme.COLOR_TEXT_LIGHT);
                btn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
            }
        }
    }

    public void restoreDashboardContent() {
        contentContainer.getChildren().clear();
        DashboardView fresh = new DashboardView();
        contentContainer.getChildren().add(fresh.getContentContainer().getChildren().get(0));
    }

    public StackPane getContentContainer() {
        return contentContainer;
    }

    public Parent getRootNode() {
        return rootPane;
    }

    public Button getNavDashboardBtn() {
        return navDashboardBtn;
    }

    public Button getNavVehiclesBtn() {
        return navVehiclesBtn;
    }

    public Button getNavBookingsBtn() {
        return navBookingsBtn;
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

    public Button getLogoutBtn() {
        return logoutBtn;
    }

    public void setAdminGreeting(String username) {
        adminGreetingLabel.setText("Dashboard — " + username);
    }
}

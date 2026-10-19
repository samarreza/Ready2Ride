package com.ready2ride.desktop.view;

import com.ready2ride.desktop.util.UiTheme;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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

import java.util.ArrayList;
import java.util.List;

/**
 * View presentation for the Ready2Ride Administrator Dashboard preview.
 * Implements a modern navigation sidebar, responsive metric summary cards,
 * fleet highlight cards, and a recent bookings TableView using pure JavaFX controls.
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
    private final TableView<BookingTableRow> bookingsTable;

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
        navVehiclesBtn = createSidebarNavButton("🚗  Vehicles  (Preview)", false);
        navBookingsBtn = createSidebarNavButton("📅  Bookings  (Preview)", false);
        navCustomersBtn = createSidebarNavButton("👥  Customers (Preview)", false);
        navPaymentsBtn = createSidebarNavButton("💳  Payments  (Preview)", false);
        navMaintenanceBtn = createSidebarNavButton("🔧  Maintenance (Preview)", false);

        allNavButtons.add(navDashboardBtn);
        allNavButtons.add(navVehiclesBtn);
        allNavButtons.add(navBookingsBtn);
        allNavButtons.add(navCustomersBtn);
        allNavButtons.add(navPaymentsBtn);
        allNavButtons.add(navMaintenanceBtn);

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

        VBox demoBadgeBox = new VBox(4);
        demoBadgeBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_PANEL, 8));
        demoBadgeBox.setPadding(new Insets(10));
        Label demoTag = new Label("PROTOTYPE PREVIEW");
        demoTag.setFont(UiTheme.FONT_SMALL);
        demoTag.setTextFill(UiTheme.COLOR_PRIMARY_BORDER);
        Label demoDesc = new Label("Frontend UI demonstration • Not connected to live DB");
        demoDesc.setFont(UiTheme.FONT_SMALL);
        demoDesc.setTextFill(UiTheme.COLOR_TEXT_HINT);
        demoDesc.setWrapText(true);
        demoBadgeBox.getChildren().addAll(demoTag, demoDesc);

        logoutBtn = new Button("🚪  Logout");
        logoutBtn.setFont(UiTheme.FONT_BODY_BOLD);
        logoutBtn.setTextFill(Color.web("#F87171")); // Soft red
        logoutBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_PANEL, 8));
        logoutBtn.setBorder(UiTheme.strokeBorder(Color.web("#7F1D1D"), 8, 1));
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setPrefHeight(38);
        logoutBtn.setCursor(Cursor.HAND);

        logoutBtn.setOnMouseEntered(e -> logoutBtn.setBackground(UiTheme.roundedBackground(Color.web("#7F1D1D"), 8)));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_NAVY_PANEL, 8)));

        sidebar.getChildren().addAll(brandHeader, navMenu, sidebarSpacer, demoBadgeBox, logoutBtn);
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

        Label subHeaderLabel = new Label("•  Fleet Management Preview");
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

        Label profileName = new Label("System Admin");
        profileName.setFont(UiTheme.FONT_BODY_BOLD);
        profileName.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Circle statusDot = new Circle(4, UiTheme.COLOR_SUCCESS);
        Label statusText = new Label("Active");
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

        // Create Default Dashboard Content
        VBox dashboardContent = new VBox(24);
        dashboardContent.setPadding(new Insets(28, 32, 32, 32));

        // A. Notice Banner
        VBox noticeBanner = new VBox(4);
        noticeBanner.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 12));
        noticeBanner.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 12, 1));
        noticeBanner.setPadding(new Insets(14, 18, 14, 18));

        Label noticeTitle = new Label("ℹ️  Sample Data Demonstration Mode");
        noticeTitle.setFont(UiTheme.FONT_BODY_BOLD);
        noticeTitle.setTextFill(Color.web("#1E40AF"));

        Label noticeBody = new Label("This dashboard is currently rendering sample fleet entities and reservations in memory. Live PostgreSQL database connectivity and binding will be finalized in upcoming phases.");
        noticeBody.setFont(UiTheme.FONT_BODY);
        noticeBody.setTextFill(Color.web("#1E3A8A"));
        noticeBody.setWrapText(true);
        noticeBanner.getChildren().addAll(noticeTitle, noticeBody);

        // B. Summary Metric Cards
        HBox metricsRow = new HBox(18);
        metricsRow.getChildren().addAll(
                createMetricCard("Total Vehicles", "24", "4 Categories registered", UiTheme.COLOR_PRIMARY, UiTheme.COLOR_PRIMARY_LIGHT),
                createMetricCard("Available Vehicles", "18", "Ready for immediate rental", UiTheme.COLOR_SUCCESS, UiTheme.COLOR_SUCCESS_LIGHT),
                createMetricCard("Active Bookings", "6", "Current customer reservations", UiTheme.COLOR_WARNING, UiTheme.COLOR_WARNING_LIGHT),
                createMetricCard("Pending Maintenance", "2", "Scheduled safety inspections", UiTheme.COLOR_ERROR, UiTheme.COLOR_ERROR_LIGHT)
        );
        HBox.setHgrow(metricsRow, Priority.ALWAYS);

        // C. Fleet Highlights (Inspired by Image 1)
        Label fleetSectionTitle = new Label("Fleet Highlights (Demonstrating Polymorphic Subclasses)");
        fleetSectionTitle.setFont(UiTheme.FONT_HEADER);
        fleetSectionTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        HBox vehicleCardsRow = new HBox(16);
        vehicleCardsRow.getChildren().addAll(
                createVehicleCard("Honda City (2024)", "CAR", "5 Seats • Petrol", "$55.00 / day", true),
                createVehicleCard("Royal Enfield Hunter 350", "BIKE", "349 cc • Roadster", "$15.00 / hr", true),
                createVehicleCard("Mahindra Scorpio-N", "SUV", "7 Seats • 4WD", "$85.00 / day", false),
                createVehicleCard("Force Traveller", "VAN", "1500 kg Cargo Capacity", "$110.00 / day", true)
        );

        // D. Recent Bookings Table
        Label tableSectionTitle = new Label("Recent Bookings");
        tableSectionTitle.setFont(UiTheme.FONT_HEADER);
        tableSectionTitle.setTextFill(UiTheme.COLOR_TEXT_DARK);

        bookingsTable = createRecentBookingsTable();

        dashboardContent.getChildren().addAll(
                noticeBanner,
                metricsRow,
                fleetSectionTitle,
                vehicleCardsRow,
                tableSectionTitle,
                bookingsTable
        );

        ScrollPane scrollPane = new ScrollPane(dashboardContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        scrollPane.setBorder(Border.EMPTY);

        contentContainer.getChildren().add(scrollPane);
        rootPane.setCenter(contentContainer);
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

    private VBox createMetricCard(String title, String value, String subtitle, Color accentColor, Color bgAccent) {
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

    private VBox createVehicleCard(String title, String type, String spec, String rate, boolean available) {
        VBox card = new VBox(10);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setPrefWidth(240);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        card.setEffect(UiTheme.subtleShadow());

        // Header: Type badge & Status pill
        Label typeBadge = UiTheme.createStatusBadge(type, UiTheme.COLOR_PRIMARY, UiTheme.COLOR_PRIMARY_LIGHT, UiTheme.COLOR_PRIMARY_BORDER);
        Label statusBadge = available
                ? UiTheme.createStatusBadge("Available", UiTheme.COLOR_SUCCESS, UiTheme.COLOR_SUCCESS_LIGHT, Color.web("#A7F3D0"))
                : UiTheme.createStatusBadge("Booked", UiTheme.COLOR_WARNING, UiTheme.COLOR_WARNING_LIGHT, Color.web("#FDE68A"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(typeBadge, spacer, statusBadge);
        topRow.setAlignment(Pos.CENTER);

        // Graphic Placeholder Rectangle (representing vehicle illustration cleanly)
        Rectangle placeholderGraphic = new Rectangle(200, 60);
        placeholderGraphic.setArcWidth(12);
        placeholderGraphic.setArcHeight(12);
        placeholderGraphic.setFill(UiTheme.COLOR_BACKGROUND_LIGHT);
        placeholderGraphic.setStroke(UiTheme.COLOR_BORDER_LIGHT);

        Label nameLabel = new Label(title);
        nameLabel.setFont(UiTheme.FONT_BODY_BOLD);
        nameLabel.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label specLabel = new Label(spec);
        specLabel.setFont(UiTheme.FONT_SMALL);
        specLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Label rateLabel = new Label(rate);
        rateLabel.setFont(UiTheme.FONT_BODY_BOLD);
        rateLabel.setTextFill(UiTheme.COLOR_PRIMARY);

        card.getChildren().addAll(topRow, placeholderGraphic, nameLabel, specLabel, rateLabel);
        return card;
    }

    private TableView<BookingTableRow> createRecentBookingsTable() {
        TableView<BookingTableRow> table = new TableView<>();
        table.setPrefHeight(230);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));
        table.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        table.setEffect(UiTheme.subtleShadow());

        TableColumn<BookingTableRow, String> colId = new TableColumn<>("Booking ID");
        colId.setCellValueFactory(cellData -> cellData.getValue().id());
        colId.setPrefWidth(110);

        TableColumn<BookingTableRow, String> colVehicle = new TableColumn<>("Vehicle");
        colVehicle.setCellValueFactory(cellData -> cellData.getValue().vehicle());
        colVehicle.setPrefWidth(180);

        TableColumn<BookingTableRow, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(cellData -> cellData.getValue().customer());
        colCustomer.setPrefWidth(140);

        TableColumn<BookingTableRow, String> colPickup = new TableColumn<>("Pickup Date");
        colPickup.setCellValueFactory(cellData -> cellData.getValue().pickup());
        colPickup.setPrefWidth(130);

        TableColumn<BookingTableRow, String> colReturn = new TableColumn<>("Return Date");
        colReturn.setCellValueFactory(cellData -> cellData.getValue().returnTime());
        colReturn.setPrefWidth(130);

        TableColumn<BookingTableRow, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amount());
        colAmount.setPrefWidth(90);

        TableColumn<BookingTableRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().status());
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colId, colVehicle, colCustomer, colPickup, colReturn, colAmount, colStatus);

        ObservableList<BookingTableRow> sampleData = FXCollections.observableArrayList(
                new BookingTableRow("BKG-DEV-5001", "Honda City (CAR)", "Rahul Sharma", "2026-11-01 10:00", "2026-11-03 10:00", "$110.00", "CONFIRMED"),
                new BookingTableRow("BKG-DEV-5002", "Royal Enfield (BIKE)", "Priya Patel", "2026-11-01 14:00", "2026-11-01 17:00", "$45.00", "CONFIRMED"),
                new BookingTableRow("BKG-DEV-5003", "Mahindra Scorpio-N (SUV)", "Amit Verma", "2026-11-04 09:00", "2026-11-06 09:00", "$170.00", "CONFIRMED"),
                new BookingTableRow("BKG-DEV-5004", "Force Traveller (VAN)", "Logistics Corp", "2026-11-05 08:00", "2026-11-07 08:00", "$220.00", "PENDING")
        );
        table.setItems(sampleData);
        return table;
    }

    /**
     * Renders a preview placeholder view for the upcoming module sections
     * (Vehicles, Bookings, Customers, Payments, Maintenance).
     */
    public void showModulePreview(String moduleName, String description, Runnable onBack) {
        VBox previewBox = new VBox(20);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.setPadding(new Insets(48));

        VBox card = new VBox(16);
        card.setMaxWidth(560);
        card.setAlignment(Pos.CENTER);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 20));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 20, 1));
        card.setPadding(new Insets(36));
        card.setEffect(UiTheme.cardShadow());

        StackPane icon = UiTheme.createBrandLogo(48);

        Label title = new Label(moduleName + " Module (Preview)");
        title.setFont(UiTheme.FONT_TITLE);
        title.setTextFill(UiTheme.COLOR_TEXT_DARK);

        Label badge = UiTheme.createStatusBadge("SCHEDULED FOR PHASE 4", UiTheme.COLOR_PRIMARY, UiTheme.COLOR_PRIMARY_LIGHT, UiTheme.COLOR_PRIMARY_BORDER);

        Label desc = new Label(description);
        desc.setFont(UiTheme.FONT_BODY);
        desc.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        Button backBtn = UiTheme.createPrimaryButton("← Return to Dashboard");
        backBtn.setOnAction(e -> onBack.run());

        card.getChildren().addAll(icon, badge, title, desc, backBtn);
        previewBox.getChildren().add(card);

        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(previewBox);
    }

    public void restoreDashboardContent() {
        contentContainer.getChildren().clear();
        // Restore default dashboard content
        DashboardView fresh = new DashboardView();
        contentContainer.getChildren().add(fresh.getContentContainer().getChildren().get(0));
    }

    public StackPane getContentContainer() {
        return contentContainer;
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

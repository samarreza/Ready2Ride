package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.model.VehicleType;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Vehicle Listing and Filtering View matching Screenshot 1 reference.
 * Features:
 * - Top navigation header with active Vehicles pill
 * - Scenic dark sunset hero banner "Explore Our Vehicles"
 * - Left filter sidebar: Vehicle Types, Price Range Slider, Fuel Types, Seating Capacities
 * - Search & Sorting toolbar with real-time count
 * - 4-column responsive vehicle cards with category badges, favorite hearts, PostgreSQL BYTEA images, specs, and View Details
 * - Bottom pagination control (1, 2, 3, 4, 5)
 */
public class VehicleCatalogView {

    private final BorderPane rootPane;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final FavoritesService favoritesService;

    private final Runnable onBackToHome;
    private final Runnable onOpenLogin;
    private final Runnable onOpenRegister;
    private final Runnable onOpenMyBookings;

    // Filters
    private final CheckBox carCheckBox = new CheckBox("Cars");
    private final CheckBox bikeCheckBox = new CheckBox("Bikes");
    private final CheckBox suvCheckBox = new CheckBox("SUVs");
    private final CheckBox vanCheckBox = new CheckBox("Vans");

    private final Slider priceSlider = new Slider(500, 10000, 10000);
    private final Label priceSliderValueLabel = new Label("₹ 10,000");

    private final CheckBox petrolCheckBox = new CheckBox("Petrol");
    private final CheckBox dieselCheckBox = new CheckBox("Diesel");
    private final CheckBox electricCheckBox = new CheckBox("Electric");
    private final CheckBox cngCheckBox = new CheckBox("CNG");

    private final CheckBox seat12CheckBox = new CheckBox("1 - 2");
    private final CheckBox seat35CheckBox = new CheckBox("3 - 5");
    private final CheckBox seat68CheckBox = new CheckBox("6 - 8");
    private final CheckBox seat9PlusCheckBox = new CheckBox("9+");

    // Search and Sort
    private final TextField searchField = new TextField();
    private final ComboBox<String> sortCombo = new ComboBox<>();
    private final Label resultCountLabel = new Label("0 Vehicles Found");

    private final FlowPane cardsGrid = new FlowPane();
    private final HBox paginationRow = new HBox(8);
    private int currentPage = 1;
    private final int itemsPerPage = 8;

    private List<Vehicle> allVehicles = new ArrayList<>();
    private List<Vehicle> filteredVehicles = new ArrayList<>();
    private final Map<String, byte[]> imageByteCache = new ConcurrentHashMap<>();
    private final Set<String> favoriteIds = new HashSet<>();

    private LocalDate searchPickupDate;
    private LocalDate searchReturnDate;
    private String searchLocation;

    public VehicleCatalogView(VehicleService vehicleService, BookingService bookingService,
                              PaymentService paymentService, FavoritesService favoritesService,
                              Runnable onBackToHome, Runnable onOpenLogin, Runnable onOpenRegister,
                              Runnable onOpenMyBookings,
                              VehicleType initialFilter, LocalDate searchPickup, LocalDate searchReturn, String searchLocation) {
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.favoritesService = favoritesService;
        this.onBackToHome = onBackToHome;
        this.onOpenLogin = onOpenLogin;
        this.onOpenRegister = onOpenRegister;
        this.onOpenMyBookings = onOpenMyBookings;
        this.searchPickupDate = searchPickup;
        this.searchReturnDate = searchReturn;
        this.searchLocation = searchLocation;

        rootPane = new BorderPane();
        rootPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));

        // If an initial filter was passed, set checkbox
        if (initialFilter != null) {
            switch (initialFilter) {
                case CAR -> carCheckBox.setSelected(true);
                case BIKE -> bikeCheckBox.setSelected(true);
                case SUV -> suvCheckBox.setSelected(true);
                case VAN -> vanCheckBox.setSelected(true);
            }
        }

        // 1. Top Navigation Bar (Header)
        HBox header = buildHeader();

        // 2. Main Scrollable Container (Hero + Body)
        VBox scrollContent = new VBox(0);

        // Hero Section (Screenshot 1)
        StackPane heroSection = buildHeroSection();

        // Catalog Body: Filter Sidebar (Left) + Search & Grid (Right)
        HBox catalogBody = buildCatalogBody();

        scrollContent.getChildren().addAll(heroSection, catalogBody);

        ScrollPane mainScroll = new ScrollPane(scrollContent);
        mainScroll.setFitToWidth(true);
        mainScroll.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BACKGROUND_LIGHT));
        mainScroll.setBorder(Border.EMPTY);

        rootPane.setTop(header);
        rootPane.setCenter(mainScroll);

        loadInitialData();
    }

    public VehicleCatalogView(VehicleService vehicleService, BookingService bookingService,
                              Runnable onBackToHome, VehicleType initialFilter,
                              LocalDate searchPickup, LocalDate searchReturn, String searchLocation) {
        this(vehicleService, bookingService, null, null, onBackToHome, null, null, null, initialFilter, searchPickup, searchReturn, searchLocation);
    }

    private HBox buildHeader() {
        HBox bar = new HBox(16);
        bar.setPrefHeight(72);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 48, 10, 48));
        bar.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        bar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 0, 1));
        bar.setEffect(UiTheme.subtleShadow());

        // Brand Logo
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setCursor(Cursor.HAND);
        brand.setOnMouseClicked(e -> {
            if (onBackToHome != null) onBackToHome.run();
        });

        brand.getChildren().addAll(
                UiTheme.createBrandLogo(36),
                new VBox(1,
                        new Label("Ready2Ride") {{
                            setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
                            setTextFill(UiTheme.COLOR_NAVY_DARK);
                        }},
                        new Label("RENT • RIDE • EXPLORE") {{
                            setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 8));
                            setTextFill(UiTheme.COLOR_TEXT_MUTED);
                        }}
                )
        );

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Nav Links: Home, Vehicles [Active Pill], About Us, Why Choose Us, Contact
        Button homeNav = UiTheme.createGhostButton("Home");
        homeNav.setOnAction(e -> {
            if (onBackToHome != null) onBackToHome.run();
        });

        Button vehiclesNav = new Button("Vehicles");
        vehiclesNav.setFont(UiTheme.FONT_BODY_BOLD);
        vehiclesNav.setTextFill(UiTheme.COLOR_WHITE);
        vehiclesNav.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
        vehiclesNav.setBorder(Border.EMPTY);
        vehiclesNav.setPadding(new Insets(8, 18, 8, 18));
        vehiclesNav.setCursor(Cursor.HAND);

        Button aboutNav = UiTheme.createGhostButton("About Us");
        aboutNav.setOnAction(e -> InfoDialogs.showAboutUsDialog());

        Button whyNav = UiTheme.createGhostButton("Why Choose Us");
        whyNav.setOnAction(e -> InfoDialogs.showWhyChooseUsDialog());

        Button contactNav = UiTheme.createGhostButton("Contact");
        contactNav.setOnAction(e -> InfoDialogs.showContactDialog());

        HBox navItems = new HBox(8, homeNav, vehiclesNav, aboutNav, whyNav, contactNav);
        navItems.setAlignment(Pos.CENTER_LEFT);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Auth Buttons or Profile Chip
        HBox authBox = new HBox(10);
        authBox.setAlignment(Pos.CENTER_RIGHT);

        if (UserSession.getInstance().isLoggedIn()) {
            HBox userChip = new HBox(8);
            userChip.setAlignment(Pos.CENTER_LEFT);
            userChip.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 16));
            userChip.setPadding(new Insets(6, 12, 6, 8));
            userChip.setCursor(Cursor.HAND);

            Circle circle = new Circle(12, UiTheme.COLOR_PRIMARY);
            Label name = new Label(UserSession.getInstance().getDisplayName());
            name.setFont(UiTheme.FONT_BODY_BOLD);
            name.setTextFill(UiTheme.COLOR_PRIMARY);

            userChip.getChildren().addAll(circle, name);
            userChip.setOnMouseClicked(e -> {
                if (onOpenMyBookings != null) onOpenMyBookings.run();
            });

            Button logoutBtn = UiTheme.createGhostButton("Logout");
            logoutBtn.setOnAction(e -> {
                UserSession.getInstance().logout();
                if (onBackToHome != null) onBackToHome.run();
            });

            authBox.getChildren().addAll(userChip, logoutBtn);
        } else {
            Button loginBtn = new Button("👤  Login");
            loginBtn.setFont(UiTheme.FONT_BODY_BOLD);
            loginBtn.setTextFill(UiTheme.COLOR_PRIMARY);
            loginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            loginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1));
            loginBtn.setPadding(new Insets(7, 16, 7, 16));
            loginBtn.setCursor(Cursor.HAND);
            loginBtn.setOnAction(e -> {
                if (onOpenLogin != null) onOpenLogin.run();
            });

            Button regBtn = new Button("+ Register");
            regBtn.setFont(UiTheme.FONT_BODY_BOLD);
            regBtn.setTextFill(UiTheme.COLOR_WHITE);
            regBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
            regBtn.setBorder(Border.EMPTY);
            regBtn.setPadding(new Insets(8, 18, 8, 18));
            regBtn.setCursor(Cursor.HAND);
            regBtn.setOnAction(e -> {
                if (onOpenRegister != null) onOpenRegister.run();
            });

            authBox.getChildren().addAll(loginBtn, regBtn);
        }

        bar.getChildren().addAll(brand, spacer1, navItems, spacer2, authBox);
        return bar;
    }

    private StackPane buildHeroSection() {
        StackPane hero = new StackPane();
        hero.setPrefHeight(180);
        hero.setMaxHeight(180);

        ImageView iv = new ImageView();
        iv.setPreserveRatio(false);
        iv.fitWidthProperty().bind(hero.widthProperty());
        iv.fitHeightProperty().bind(hero.heightProperty());

        try (InputStream in = getClass().getResourceAsStream("/images/hero_sunset.jpg")) {
            if (in != null) {
                iv.setImage(new Image(in));
            }
        } catch (Exception ignored) {
        }

        Rectangle darkMask = new Rectangle();
        darkMask.widthProperty().bind(hero.widthProperty());
        darkMask.heightProperty().bind(hero.heightProperty());
        darkMask.setFill(Color.rgb(10, 18, 40, 0.70));

        VBox textBox = new VBox(6);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPadding(new Insets(30, 48, 30, 48));

        Label title = new Label("Explore Our Vehicles");
        title.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 36));
        title.setTextFill(UiTheme.COLOR_WHITE);

        Label sub = new Label("A wide range of well-maintained vehicles for every journey.");
        sub.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 15));
        sub.setTextFill(Color.web("#E2E8F0"));

        textBox.getChildren().addAll(title, sub);
        hero.getChildren().addAll(iv, darkMask, textBox);
        return hero;
    }

    private HBox buildCatalogBody() {
        HBox body = new HBox(28);
        body.setPadding(new Insets(24, 48, 40, 48));
        body.setAlignment(Pos.TOP_LEFT);

        // --- Left Sidebar: Filters ---
        VBox filterSidebar = buildFilterSidebar();

        // --- Right Section: Search & Sort Bar, Vehicle Cards Grid, Pagination ---
        VBox rightSection = buildMainCatalogSection();
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        body.getChildren().addAll(filterSidebar, rightSection);
        return body;
    }

    private VBox buildFilterSidebar() {
        VBox sidebar = new VBox(18);
        sidebar.setPrefWidth(260);
        sidebar.setMinWidth(240);
        sidebar.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        sidebar.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        sidebar.setPadding(new Insets(20));
        sidebar.setEffect(UiTheme.subtleShadow());

        // Header
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        Label filterTitle = new Label("Filters");
        filterTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 17));
        filterTitle.setTextFill(UiTheme.COLOR_NAVY_DARK);
        head.getChildren().addAll(UiTheme.createSvgIcon("M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z", 18, UiTheme.COLOR_PRIMARY), filterTitle);

        // Group 1: Vehicle Type
        VBox typeGroup = createFilterGroup("Vehicle Type", carCheckBox, bikeCheckBox, suvCheckBox, vanCheckBox);

        // Group 2: Price Range Slider
        VBox priceGroup = new VBox(8);
        Label pHead = new Label("Price Range (Per Day)");
        pHead.setFont(UiTheme.FONT_BODY_BOLD);
        pHead.setTextFill(UiTheme.COLOR_NAVY_DARK);

        priceSlider.setShowTickLabels(false);
        priceSlider.setShowTickMarks(false);
        priceSlider.setBlockIncrement(500);

        priceSliderValueLabel.setFont(UiTheme.FONT_PRICE_BADGE);
        priceSliderValueLabel.setTextFill(UiTheme.COLOR_PRIMARY);

        HBox sliderMinMax = new HBox();
        Label minLbl = new Label("₹ 500");
        minLbl.setFont(UiTheme.FONT_SMALL);
        minLbl.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Region slSpacer = new Region();
        HBox.setHgrow(slSpacer, Priority.ALWAYS);

        HBox minMaxRow = new HBox(minLbl, slSpacer, priceSliderValueLabel);
        minMaxRow.setAlignment(Pos.CENTER_LEFT);

        priceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            priceSliderValueLabel.setText(String.format("₹ %,.0f", newVal.doubleValue()));
        });

        priceGroup.getChildren().addAll(pHead, priceSlider, minMaxRow);

        // Group 3: Fuel Type
        VBox fuelGroup = createFilterGroup("Fuel Type", petrolCheckBox, dieselCheckBox, electricCheckBox, cngCheckBox);

        // Group 4: Seating Capacity
        VBox seatGroup = createFilterGroup("Seating Capacity", seat12CheckBox, seat35CheckBox, seat68CheckBox, seat9PlusCheckBox);

        // Buttons: Apply Filters & Reset Filters
        Button applyBtn = new Button("🔍  Apply Filters");
        applyBtn.setFont(UiTheme.FONT_BODY_BOLD);
        applyBtn.setTextFill(UiTheme.COLOR_WHITE);
        applyBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
        applyBtn.setBorder(Border.EMPTY);
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setPrefHeight(38);
        applyBtn.setCursor(Cursor.HAND);
        applyBtn.setOnAction(e -> applyFilters());

        Button resetBtn = new Button("↻  Reset Filters");
        resetBtn.setFont(UiTheme.FONT_BODY_BOLD);
        resetBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        resetBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        resetBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1));
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setPrefHeight(36);
        resetBtn.setCursor(Cursor.HAND);
        resetBtn.setOnAction(e -> resetFilters());

        sidebar.getChildren().addAll(
                head,
                createDivider(),
                typeGroup,
                createDivider(),
                priceGroup,
                createDivider(),
                fuelGroup,
                createDivider(),
                seatGroup,
                createDivider(),
                applyBtn,
                resetBtn
        );

        return sidebar;
    }

    private VBox createFilterGroup(String title, CheckBox... boxes) {
        VBox grp = new VBox(8);
        Label lbl = new Label(title);
        lbl.setFont(UiTheme.FONT_BODY_BOLD);
        lbl.setTextFill(UiTheme.COLOR_NAVY_DARK);
        grp.getChildren().add(lbl);

        for (CheckBox cb : boxes) {
            cb.setFont(UiTheme.FONT_BODY);
            cb.setTextFill(UiTheme.COLOR_TEXT_MUTED);
            cb.setCursor(Cursor.HAND);
            cb.setOnAction(e -> applyFilters());
            grp.getChildren().add(cb);
        }
        return grp;
    }

    private Line createDivider() {
        Line l = new Line(0, 0, 220, 0);
        l.setStroke(UiTheme.COLOR_BORDER_LIGHT);
        return l;
    }

    private VBox buildMainCatalogSection() {
        VBox section = new VBox(20);

        // Top Toolbar: Search Input, Sort Dropdown, Results Count
        HBox toolbar = new HBox(16);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Search Field with icon
        searchField.setPromptText("Search vehicles by name, type, or features...");
        searchField.setFont(UiTheme.FONT_BODY);
        searchField.setPrefHeight(42);
        searchField.setPrefWidth(380);
        searchField.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        searchField.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1));
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Sort Dropdown (Screenshot 1)
        HBox sortBox = new HBox(8);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        Label sortLabel = new Label("Sort by:");
        sortLabel.setFont(UiTheme.FONT_BODY);
        sortLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        sortCombo.getItems().addAll("Price: Low to High", "Price: High to Low", "Brand / Model A-Z");
        sortCombo.setValue("Price: Low to High");
        sortCombo.setPrefHeight(38);
        sortCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        sortBox.getChildren().addAll(sortLabel, sortCombo);

        resultCountLabel.setFont(UiTheme.FONT_BODY_BOLD);
        resultCountLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        toolbar.getChildren().addAll(searchField, spacer, sortBox, resultCountLabel);

        // Vehicle Cards Grid (4 columns × dynamic rows)
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);
        cardsGrid.setAlignment(Pos.TOP_LEFT);

        // Pagination Bar (Screenshot 1: < 1 2 3 4 5 >)
        paginationRow.setAlignment(Pos.CENTER);
        paginationRow.setPadding(new Insets(20, 0, 10, 0));

        section.getChildren().addAll(toolbar, cardsGrid, paginationRow);
        return section;
    }

    private void loadInitialData() {
        new Thread(() -> {
            try {
                // 1. Load fleet from PostgreSQL
                List<Vehicle> list = vehicleService.getAllVehicles();

                // 2. Load primary images from PostgreSQL BYTEA
                for (Vehicle v : list) {
                    vehicleService.getPrimaryImage(v.getId()).ifPresent(img -> {
                        if (img.getImageData() != null) {
                            imageByteCache.put(v.getId(), img.getImageData());
                        }
                    });
                }

                // 3. Load user favorites from PostgreSQL if authenticated
                if (favoritesService != null && UserSession.getInstance().isLoggedIn()) {
                    favoriteIds.addAll(favoritesService.getFavoriteVehicleIds(UserSession.getInstance().getUserId()));
                }

                Platform.runLater(() -> {
                    allVehicles = list;
                    applyFilters();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> resultCountLabel.setText("Database error: " + ex.getMessage()));
            }
        }).start();
    }

    private void applyFilters() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        double maxPrice = priceSlider.getValue();

        boolean filterCars = carCheckBox.isSelected();
        boolean filterBikes = bikeCheckBox.isSelected();
        boolean filterSuvs = suvCheckBox.isSelected();
        boolean filterVans = vanCheckBox.isSelected();
        boolean anyTypeFilter = filterCars || filterBikes || filterSuvs || filterVans;

        boolean filterPetrol = petrolCheckBox.isSelected();
        boolean filterDiesel = dieselCheckBox.isSelected();
        boolean filterElectric = electricCheckBox.isSelected();
        boolean filterCng = cngCheckBox.isSelected();
        boolean anyFuelFilter = filterPetrol || filterDiesel || filterElectric || filterCng;

        boolean filter12 = seat12CheckBox.isSelected();
        boolean filter35 = seat35CheckBox.isSelected();
        boolean filter68 = seat68CheckBox.isSelected();
        boolean filter9p = seat9PlusCheckBox.isSelected();
        boolean anySeatFilter = filter12 || filter35 || filter68 || filter9p;

        filteredVehicles = allVehicles.stream().filter(v -> {
            // Price filter
            if (v.getRentalRate().doubleValue() > maxPrice) {
                return false;
            }

            // Type filter
            if (anyTypeFilter) {
                boolean matchType = false;
                if (filterCars && v.getVehicleType() == VehicleType.CAR) matchType = true;
                if (filterBikes && v.getVehicleType() == VehicleType.BIKE) matchType = true;
                if (filterSuvs && v.getVehicleType() == VehicleType.SUV) matchType = true;
                if (filterVans && v.getVehicleType() == VehicleType.VAN) matchType = true;
                if (!matchType) return false;
            }

            // Fuel filter
            if (anyFuelFilter) {
                String fuel = getVehicleFuel(v).toLowerCase();
                boolean matchFuel = false;
                if (filterPetrol && fuel.contains("petrol")) matchFuel = true;
                if (filterDiesel && fuel.contains("diesel")) matchFuel = true;
                if (filterElectric && fuel.contains("electric")) matchFuel = true;
                if (filterCng && fuel.contains("cng")) matchFuel = true;
                if (!matchFuel) return false;
            }

            // Seating filter
            if (anySeatFilter) {
                int seats = getVehicleSeats(v);
                boolean matchSeat = false;
                if (filter12 && seats >= 1 && seats <= 2) matchSeat = true;
                if (filter35 && seats >= 3 && seats <= 5) matchSeat = true;
                if (filter68 && seats >= 6 && seats <= 8) matchSeat = true;
                if (filter9p && seats >= 9) matchSeat = true;
                if (!matchSeat) return false;
            }

            // Text search
            if (!query.isEmpty()) {
                String fullDesc = (v.getBrand() + " " + v.getModel() + " " + v.getVehicleType().getDisplayName()).toLowerCase();
                return fullDesc.contains(query);
            }

            return true;
        }).collect(Collectors.toList());

        // Sorting
        String sortVal = sortCombo.getValue();
        if ("Price: Low to High".equals(sortVal)) {
            filteredVehicles.sort(Comparator.comparing(Vehicle::getRentalRate));
        } else if ("Price: High to Low".equals(sortVal)) {
            filteredVehicles.sort(Comparator.comparing(Vehicle::getRentalRate).reversed());
        } else if ("Brand / Model A-Z".equals(sortVal)) {
            filteredVehicles.sort(Comparator.comparing(v -> v.getBrand() + " " + v.getModel()));
        }

        currentPage = 1;
        renderCurrentPage();
    }

    private void resetFilters() {
        carCheckBox.setSelected(false);
        bikeCheckBox.setSelected(false);
        suvCheckBox.setSelected(false);
        vanCheckBox.setSelected(false);

        priceSlider.setValue(10000);

        petrolCheckBox.setSelected(false);
        dieselCheckBox.setSelected(false);
        electricCheckBox.setSelected(false);
        cngCheckBox.setSelected(false);

        seat12CheckBox.setSelected(false);
        seat35CheckBox.setSelected(false);
        seat68CheckBox.setSelected(false);
        seat9PlusCheckBox.setSelected(false);

        searchField.clear();
        sortCombo.setValue("Price: Low to High");
        applyFilters();
    }

    private void renderCurrentPage() {
        cardsGrid.getChildren().clear();
        int total = filteredVehicles.size();
        resultCountLabel.setText(total + " Vehicles Found");

        if (total == 0) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(48));
            emptyBox.getChildren().addAll(
                    new Label("No vehicles match the selected criteria.") {{
                        setFont(UiTheme.FONT_TITLE);
                        setTextFill(UiTheme.COLOR_TEXT_MUTED);
                    }},
                    new Label("Try adjusting your price range or resetting the filters.") {{
                        setFont(UiTheme.FONT_BODY);
                        setTextFill(UiTheme.COLOR_TEXT_HINT);
                    }}
            );
            cardsGrid.getChildren().add(emptyBox);
            paginationRow.getChildren().clear();
            return;
        }

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, total);

        for (int i = startIndex; i < endIndex; i++) {
            Vehicle v = filteredVehicles.get(i);
            cardsGrid.getChildren().add(buildVehicleCard(v));
        }

        renderPagination(total);
    }

    private VBox buildVehicleCard(Vehicle v) {
        VBox card = new VBox(10);
        card.setPrefWidth(265);
        card.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        card.setPadding(new Insets(14));
        card.setEffect(UiTheme.subtleShadow());

        // Top Row: Category badge on left, Heart Favorite button on right
        Label catBadge = new Label(v.getVehicleType().getDisplayName());
        catBadge.setFont(UiTheme.FONT_SMALL);
        catBadge.setTextFill(UiTheme.COLOR_WHITE);
        catBadge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 6));
        catBadge.setPadding(new Insets(3, 8, 3, 8));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        boolean isFav = favoriteIds.contains(v.getId());
        Label heartBtn = new Label(isFav ? "♥" : "♡");
        heartBtn.setFont(Font.font(16));
        heartBtn.setTextFill(isFav ? Color.RED : UiTheme.COLOR_PRIMARY);
        heartBtn.setCursor(Cursor.HAND);

        heartBtn.setOnMouseClicked(e -> {
            if (favoritesService != null && UserSession.getInstance().isLoggedIn()) {
                boolean nowFav = favoritesService.toggleFavorite(UserSession.getInstance().getUserId(), v.getId());
                if (nowFav) {
                    favoriteIds.add(v.getId());
                    heartBtn.setText("♥");
                    heartBtn.setTextFill(Color.RED);
                } else {
                    favoriteIds.remove(v.getId());
                    heartBtn.setText("♡");
                    heartBtn.setTextFill(UiTheme.COLOR_PRIMARY);
                }
            } else {
                heartBtn.setText("♥");
                heartBtn.setTextFill(Color.RED);
            }
        });

        HBox topRow = new HBox(catBadge, topSpacer, heartBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Vehicle Photo loaded from PostgreSQL BYTEA
        ImageView imgView = new ImageView();
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(130);
        imgView.setFitWidth(235);

        byte[] imgBytes = imageByteCache.get(v.getId());
        if (imgBytes != null && imgBytes.length > 0) {
            try {
                imgView.setImage(new Image(new ByteArrayInputStream(imgBytes)));
            } catch (Exception ignored) {
            }
        } else {
            String fallback = switch (v.getVehicleType()) {
                case CAR -> "car_sedan.jpg";
                case BIKE -> "bike_motorcycle.jpg";
                case SUV -> "suv_dark.jpg";
                case VAN -> "van_white.jpg";
            };
            try (InputStream in = getClass().getResourceAsStream("/images/" + fallback)) {
                if (in != null) imgView.setImage(new Image(in));
            } catch (Exception ignored) {
            }
        }

        StackPane imgWrapper = new StackPane(imgView);
        imgWrapper.setPrefHeight(135);
        imgWrapper.setAlignment(Pos.CENTER);

        // Vehicle Name
        Label nameLbl = new Label(v.getBrand() + " " + v.getModel());
        nameLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        nameLbl.setTextFill(UiTheme.COLOR_NAVY_DARK);

        // Price
        HBox priceRow = new HBox(4);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        Label priceVal = new Label(String.format("₹ %,.0f", v.getRentalRate().doubleValue()));
        priceVal.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        priceVal.setTextFill(UiTheme.COLOR_NAVY_DARK);
        Label dayLbl = new Label("/ day");
        dayLbl.setFont(UiTheme.FONT_CAPTION);
        dayLbl.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        priceRow.getChildren().addAll(priceVal, dayLbl);

        // Specs: 2 rows
        HBox specs1 = new HBox(12);
        specs1.getChildren().addAll(
                createMiniSpec(UiTheme.ICON_USER, getVehicleSeats(v) + " Seats"),
                createMiniSpec(UiTheme.ICON_SHIELD, getVehicleBags(v) + " Bags")
        );

        HBox specs2 = new HBox(12);
        specs2.getChildren().addAll(
                createMiniSpec(UiTheme.ICON_RUPEE, getVehicleFuel(v)),
                createMiniSpec(UiTheme.ICON_HOW_IT_WORKS, getVehicleTransmission(v))
        );

        // View Details Button (Screenshot 1)
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setFont(UiTheme.FONT_BODY_BOLD);
        viewDetailsBtn.setTextFill(UiTheme.COLOR_PRIMARY);
        viewDetailsBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        viewDetailsBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1));
        viewDetailsBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailsBtn.setPrefHeight(36);
        viewDetailsBtn.setCursor(Cursor.HAND);

        viewDetailsBtn.setOnAction(e -> openDetailsModal(v));

        card.getChildren().addAll(topRow, imgWrapper, nameLbl, priceRow, specs1, specs2, viewDetailsBtn);

        card.setOnMouseEntered(e -> {
            card.setEffect(UiTheme.cardShadow());
            card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 16, 1.5));
        });
        card.setOnMouseExited(e -> {
            card.setEffect(UiTheme.subtleShadow());
            card.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        });

        return card;
    }

    private void openDetailsModal(Vehicle v) {
        VehicleDetailsModal modal = new VehicleDetailsModal(
                v,
                imageByteCache.get(v.getId()),
                searchPickupDate,
                searchReturnDate,
                searchLocation,
                bookingService,
                paymentService,
                onOpenMyBookings
        );
        modal.showAndWait();
    }

    private void renderPagination(int total) {
        paginationRow.getChildren().clear();
        int pages = (int) Math.ceil((double) total / itemsPerPage);
        if (pages <= 1) return;

        Button prevBtn = new Button("‹");
        prevBtn.setFont(Font.font(14));
        prevBtn.setDisable(currentPage == 1);
        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderCurrentPage();
            }
        });
        paginationRow.getChildren().add(prevBtn);

        for (int p = 1; p <= pages; p++) {
            final int pageNum = p;
            Button pageBtn = new Button(String.valueOf(pageNum));
            pageBtn.setFont(UiTheme.FONT_BODY_BOLD);
            pageBtn.setPrefSize(34, 34);

            if (pageNum == currentPage) {
                pageBtn.setTextFill(UiTheme.COLOR_WHITE);
                pageBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 6));
                pageBtn.setBorder(Border.EMPTY);
            } else {
                pageBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
                pageBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 6));
                pageBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 6, 1));
            }

            pageBtn.setOnAction(e -> {
                currentPage = pageNum;
                renderCurrentPage();
            });
            paginationRow.getChildren().add(pageBtn);
        }

        Button nextBtn = new Button("›");
        nextBtn.setFont(Font.font(14));
        nextBtn.setDisable(currentPage == pages);
        nextBtn.setOnAction(e -> {
            if (currentPage < pages) {
                currentPage++;
                renderCurrentPage();
            }
        });
        paginationRow.getChildren().add(nextBtn);
    }

    private HBox createMiniSpec(String iconSvg, String text) {
        HBox r = new HBox(6);
        r.setAlignment(Pos.CENTER_LEFT);
        r.getChildren().addAll(
                UiTheme.createSvgIcon(iconSvg, 12, UiTheme.COLOR_TEXT_MUTED),
                new Label(text) {{
                    setFont(UiTheme.FONT_CAPTION);
                    setTextFill(UiTheme.COLOR_TEXT_MUTED);
                }}
        );
        return r;
    }

    private int getVehicleSeats(Vehicle v) {
        if (v instanceof Car c) return c.getSeatingCapacity();
        if (v instanceof Bike b) return b.getSeatingCapacity();
        if (v instanceof SUV s) return s.getSeatingCapacity();
        if (v instanceof Van va) return va.getSeatingCapacity();
        return 4;
    }

    private int getVehicleBags(Vehicle v) {
        if (v instanceof Car c) return c.getLuggageCapacityBags();
        if (v instanceof SUV s) return s.getLuggageCapacityBags();
        if (v instanceof Van va) return va.getLuggageCapacityBags();
        return 0;
    }

    private String getVehicleFuel(Vehicle v) {
        if (v instanceof Car c) return c.getFuelType();
        if (v instanceof Bike b) return b.getFuelType();
        if (v instanceof SUV s) return s.getFuelType();
        if (v instanceof Van va) return va.getFuelType();
        return "Petrol";
    }

    private String getVehicleTransmission(Vehicle v) {
        if (v instanceof Car) return "Automatic";
        if (v instanceof SUV) return "Automatic";
        return "Manual";
    }

    public BorderPane getRootNode() {
        return rootPane;
    }
}

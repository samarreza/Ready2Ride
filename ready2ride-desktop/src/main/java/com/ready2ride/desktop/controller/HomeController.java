package com.ready2ride.desktop.controller;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.model.VehicleImage;
import com.ready2ride.core.model.VehicleType;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.util.UiTheme;
import com.ready2ride.desktop.view.CancellationConfirmationDialog;
import com.ready2ride.desktop.view.HomeView;
import com.ready2ride.desktop.view.InfoDialogs;
import com.ready2ride.desktop.view.RegisterDialog;
import javafx.application.Platform;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller orchestrating all interactions, validations, and PostgreSQL data loading
 * for the Ready2Ride Home Page.
 */
public class HomeController {

    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    private final HomeView view;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final AuthenticationService authService;
    private final Consumer<VehicleType> onOpenVehicleCatalog;
    private final SearchNavigator onSearchVehicleCatalog;
    private final Runnable onNavigateToLogin;
    private final Runnable onNavigateToRegister;

    private final Runnable onOpenMyBookings;
    private final Runnable onNavigateToAboutUs;

    @FunctionalInterface
    public interface SearchNavigator {
        void navigate(VehicleType type, LocalDate pickup, LocalDate returnDate, String location);
    }

    public HomeController(HomeView view,
                          VehicleService vehicleService,
                          BookingService bookingService,
                          AuthenticationService authService,
                          Consumer<VehicleType> onOpenVehicleCatalog,
                          SearchNavigator onSearchVehicleCatalog,
                          Runnable onNavigateToLogin,
                          Runnable onNavigateToRegister,
                          Runnable onOpenMyBookings,
                          Runnable onNavigateToAboutUs) {
        this.view = Objects.requireNonNull(view, "HomeView must not be null");
        this.vehicleService = Objects.requireNonNull(vehicleService, "VehicleService must not be null");
        this.bookingService = Objects.requireNonNull(bookingService, "BookingService must not be null");
        this.authService = authService;
        this.onOpenVehicleCatalog = Objects.requireNonNull(onOpenVehicleCatalog, "Catalog navigation callback must not be null");
        this.onSearchVehicleCatalog = onSearchVehicleCatalog;
        this.onNavigateToLogin = Objects.requireNonNull(onNavigateToLogin, "Login navigation callback must not be null");
        this.onNavigateToRegister = onNavigateToRegister;
        this.onOpenMyBookings = onOpenMyBookings;
        this.onNavigateToAboutUs = onNavigateToAboutUs;

        initEventHandlers();
        loadHomeDataFromDatabase();
    }

    public HomeController(HomeView view,
                          VehicleService vehicleService,
                          BookingService bookingService,
                          AuthenticationService authService,
                          Consumer<VehicleType> onOpenVehicleCatalog,
                          SearchNavigator onSearchVehicleCatalog,
                          Runnable onNavigateToLogin,
                          Runnable onNavigateToRegister,
                          Runnable onOpenMyBookings) {
        this(view, vehicleService, bookingService, authService, onOpenVehicleCatalog,
             onSearchVehicleCatalog, onNavigateToLogin, onNavigateToRegister, onOpenMyBookings, null);
    }

    public HomeController(HomeView view,
                          VehicleService vehicleService,
                          BookingService bookingService,
                          AuthenticationService authService,
                          Consumer<VehicleType> onOpenVehicleCatalog,
                          SearchNavigator onSearchVehicleCatalog,
                          Runnable onNavigateToLogin,
                          Runnable onNavigateToRegister) {
        this(view, vehicleService, bookingService, authService, onOpenVehicleCatalog,
             onSearchVehicleCatalog, onNavigateToLogin, onNavigateToRegister, null, null);
    }

    public HomeController(HomeView view,
                          VehicleService vehicleService,
                          BookingService bookingService,
                          AuthenticationService authService,
                          Consumer<VehicleType> onOpenVehicleCatalog,
                          SearchNavigator onSearchVehicleCatalog,
                          Runnable onNavigateToLogin) {
        this(view, vehicleService, bookingService, authService, onOpenVehicleCatalog,
             onSearchVehicleCatalog, onNavigateToLogin, null, null, null);
    }

    public HomeController(HomeView view,
                          VehicleService vehicleService,
                          BookingService bookingService,
                          AuthenticationService authService,
                          Consumer<VehicleType> onOpenVehicleCatalog,
                          Runnable onNavigateToLogin) {
        this(view, vehicleService, bookingService, authService, onOpenVehicleCatalog,
             (type, pickup, returnDate, loc) -> onOpenVehicleCatalog.accept(type), onNavigateToLogin, null, null, null);
    }

    private void initEventHandlers() {
        // --- Navigation Header Handlers wired through shared TopNavBar ---
        view.getTopNavBar().setOnNavigateHome(() -> view.getScrollPane().setVvalue(0));
        view.getTopNavBar().setOnNavigateVehicles(() -> onOpenVehicleCatalog.accept(null));
        view.getTopNavBar().setOnNavigateMyBookings(() -> {
            if (onOpenMyBookings != null) {
                onOpenMyBookings.run();
            }
        });
        if (onNavigateToAboutUs != null) {
            view.getTopNavBar().setOnNavigateAboutUs(onNavigateToAboutUs);
        } else if (com.ready2ride.desktop.view.TopNavBar.getDefaultOnNavigateAboutUs() != null) {
            view.getTopNavBar().setOnNavigateAboutUs(com.ready2ride.desktop.view.TopNavBar.getDefaultOnNavigateAboutUs());
        } else {
            view.getTopNavBar().setOnNavigateAboutUs(InfoDialogs::showAboutUsDialog);
        }
        view.getTopNavBar().setOnNavigateContact(InfoDialogs::showContactDialog);
        view.getTopNavBar().setOnSearch(() -> {
            view.getLocationComboBox().requestFocus();
            view.getScrollPane().setVvalue(0.12);
        });
        view.getTopNavBar().setOnLogin(onNavigateToLogin);
        view.getTopNavBar().setOnRegister(() -> {
            if (onNavigateToRegister != null) {
                onNavigateToRegister.run();
            } else if (authService != null) {
                RegisterDialog.show(authService);
            }
        });
        view.getTopNavBar().setOnLogout(onNavigateToLogin);

        // --- Booking Panel Tab Switching ---
        view.getTabRentVehicleBtn().setOnAction(e -> switchTab(true));
        view.getTabFindBookingBtn().setOnAction(e -> switchTab(false));

        // --- Rent a Vehicle Search & Validation ---
        view.getSearchVehiclesBtn().setOnAction(e -> handleSearchVehicles());

        // --- Find My Booking Lookup ---
        view.getBookingLookupBtn().setOnAction(e -> handleBookingLookup());
        view.getBookingLookupField().setOnAction(e -> handleBookingLookup());

        // --- Category Cards Click Handlers ---
        for (Map.Entry<VehicleType, HomeView.CategoryCardComponent> entry : view.getCategoryCards().entrySet()) {
            VehicleType type = entry.getKey();
            HomeView.CategoryCardComponent card = entry.getValue();
            card.getNode().setOnMouseClicked(e -> onOpenVehicleCatalog.accept(type));
            card.getActionArrowBtn().setOnAction(e -> onOpenVehicleCatalog.accept(type));
        }

        view.getViewAllVehiclesLink().setOnAction(e -> onOpenVehicleCatalog.accept(null));
    }

    private void switchTab(boolean isRentTab) {
        view.clearValidationMessage();
        if (isRentTab) {
            // Activate Rent Tab
            view.getTabRentVehicleBtn().setTextFill(UiTheme.COLOR_WHITE);
            view.getTabRentVehicleBtn().setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
            view.getTabFindBookingBtn().setTextFill(UiTheme.COLOR_TEXT_DARK);
            view.getTabFindBookingBtn().setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 10));

            view.getRentTabContentBox().setVisible(true);
            view.getRentTabContentBox().setManaged(true);
            view.getFindBookingContentBox().setVisible(false);
            view.getFindBookingContentBox().setManaged(false);
        } else {
            // Activate Find Booking Tab
            view.getTabFindBookingBtn().setTextFill(UiTheme.COLOR_WHITE);
            view.getTabFindBookingBtn().setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
            view.getTabRentVehicleBtn().setTextFill(UiTheme.COLOR_TEXT_DARK);
            view.getTabRentVehicleBtn().setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 10));

            view.getRentTabContentBox().setVisible(false);
            view.getRentTabContentBox().setManaged(false);
            view.getFindBookingContentBox().setVisible(true);
            view.getFindBookingContentBox().setManaged(true);
        }
    }

    private void handleSearchVehicles() {
        view.clearValidationMessage();

        String location = view.getLocationComboBox().getValue();
        LocalDate pickup = view.getPickupDatePicker().getValue();
        LocalDate returnDate = view.getReturnDatePicker().getValue();

        // 1. Check missing values
        if (location == null || location.isBlank()) {
            view.showValidationMessage("Please select a pickup location.");
            view.getLocationComboBox().requestFocus();
            return;
        }

        if (pickup == null) {
            view.showValidationMessage("Please select a valid pickup date.");
            view.getPickupDatePicker().requestFocus();
            return;
        }

        if (returnDate == null) {
            view.showValidationMessage("Please select a valid return date.");
            view.getReturnDatePicker().requestFocus();
            return;
        }

        // 2. Check past pickup date
        if (pickup.isBefore(LocalDate.now())) {
            view.showValidationMessage("Pickup date cannot be in the past. Please choose today or a future date.");
            view.getPickupDatePicker().requestFocus();
            return;
        }

        // 3. Check return strictly after pickup
        if (!returnDate.isAfter(pickup)) {
            view.showValidationMessage("Return date must be strictly after pickup date.");
            view.getReturnDatePicker().requestFocus();
            return;
        }

        // Validated successfully -> navigate to catalog with filtered criteria
        if (onSearchVehicleCatalog != null) {
            onSearchVehicleCatalog.navigate(null, pickup, returnDate, location);
        } else {
            onOpenVehicleCatalog.accept(null);
        }
    }

    private void handleBookingLookup() {
        String bookingId = view.getBookingLookupField().getText() != null
                ? view.getBookingLookupField().getText().trim()
                : "";

        if (bookingId.isEmpty()) {
            view.getBookingLookupResultLabel().setText("Please enter a booking reference ID.");
            view.getBookingLookupResultLabel().setTextFill(UiTheme.COLOR_ERROR);
            view.getBookingLookupResultLabel().setVisible(true);
            view.getBookingCancelBtn().setVisible(false);
            view.getBookingCancelBtn().setManaged(false);
            return;
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);
            Vehicle v = vehicleService.getVehicleById(booking.getVehicleId());

            String info = String.format("✓ Reservation Found: %s • Vehicle: %s %s • Dates: %s to %s • Status: %s • Amount: ₹ %,.2f",
                    booking.getBookingId(),
                    v.getBrand(), v.getModel(),
                    booking.getPickupDateTime().toLocalDate(),
                    booking.getReturnDateTime().toLocalDate(),
                    booking.getStatus(),
                    booking.getRentalAmount().doubleValue());

            view.getBookingLookupResultLabel().setText(info);
            view.getBookingLookupResultLabel().setTextFill(
                    booking.getStatus() == com.ready2ride.core.model.BookingStatus.CONFIRMED
                            ? UiTheme.COLOR_SUCCESS
                            : UiTheme.COLOR_TEXT_DARK
            );
            view.getBookingLookupResultLabel().setVisible(true);

            if (booking.getStatus() == com.ready2ride.core.model.BookingStatus.CONFIRMED) {
                view.getBookingCancelBtn().setVisible(true);
                view.getBookingCancelBtn().setManaged(true);
                view.getBookingCancelBtn().setOnAction(e -> handleCancelBooking(booking));
            } else {
                view.getBookingCancelBtn().setVisible(false);
                view.getBookingCancelBtn().setManaged(false);
            }
        } catch (Exception ex) {
            view.getBookingLookupResultLabel().setText("No reservation found matching ID '" + bookingId + "'. Please check the reference code.");
            view.getBookingLookupResultLabel().setTextFill(UiTheme.COLOR_ERROR);
            view.getBookingLookupResultLabel().setVisible(true);
            view.getBookingCancelBtn().setVisible(false);
            view.getBookingCancelBtn().setManaged(false);
        }
    }

    private void handleCancelBooking(Booking booking) {
        Vehicle vehicle = null;
        try {
            vehicle = vehicleService.getVehicleById(booking.getVehicleId());
        } catch (Exception ignored) {
        }
        javafx.stage.Window window = (view.getRootNode() != null && view.getRootNode().getScene() != null)
                ? view.getRootNode().getScene().getWindow() : null;

        boolean confirmed = CancellationConfirmationDialog.show(window, booking, vehicle);
        if (confirmed) {
            try {
                Booking cancelled = bookingService.cancelBooking(booking.getBookingId());
                view.getBookingLookupResultLabel().setText("✓ Reservation " + cancelled.getBookingId() + " successfully cancelled. Status: CANCELLED.");
                view.getBookingLookupResultLabel().setTextFill(UiTheme.COLOR_WARNING);
                view.getBookingCancelBtn().setVisible(false);
                view.getBookingCancelBtn().setManaged(false);

                CancellationConfirmationDialog.showNotification(
                        window,
                        "Booking Cancelled",
                        "Reservation Successfully Cancelled",
                        "Booking " + booking.getBookingId() + " has been updated to CANCELLED in PostgreSQL. The vehicle is now available for other customers.",
                        true
                );
            } catch (Exception ex) {
                view.getBookingLookupResultLabel().setText("Failed to cancel reservation: " + ex.getMessage());
                view.getBookingLookupResultLabel().setTextFill(UiTheme.COLOR_ERROR);

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

    /**
     * Loads live vehicle prices, specifications, and raw image bytes from PostgreSQL
     * in a background task and updates the JavaFX UI without blocking the UI thread.
     */
    public void loadHomeDataFromDatabase() {
        Thread thread = new Thread(() -> {
            try {
                List<Vehicle> allVehicles = vehicleService.getAllVehicles();

                for (VehicleType type : VehicleType.values()) {
                    List<Vehicle> typeVehicles = allVehicles.stream()
                            .filter(v -> v.getVehicleType() == type && v.isActive())
                            .toList();

                    if (!typeVehicles.isEmpty()) {
                        // Find starting rate
                        Vehicle representative = typeVehicles.stream()
                                .min(Comparator.comparing(Vehicle::getRentalRate))
                                .orElse(typeVehicles.get(0));

                        BigDecimal minRate = representative.getRentalRate();

                        // Load image bytes from PostgreSQL BYTEA table
                        byte[] imageBytes = null;
                        for (Vehicle v : typeVehicles) {
                            Optional<VehicleImage> imgOpt = vehicleService.getPrimaryImage(v.getId());
                            if (imgOpt.isPresent() && imgOpt.get().getImageData() != null) {
                                imageBytes = imgOpt.get().getImageData();
                                break;
                            }
                        }

                        // Decode image in background thread to avoid blocking JavaFX UI thread
                        javafx.scene.image.Image decodedImage = null;
                        if (imageBytes != null && imageBytes.length > 0) {
                            try {
                                decodedImage = new javafx.scene.image.Image(
                                        new java.io.ByteArrayInputStream(imageBytes), 400, 300, true, true);
                            } catch (Exception ignored) {
                            }
                        }

                        final javafx.scene.image.Image fxImage = decodedImage;
                        final Vehicle finalRep = representative;
                        final BigDecimal finalRate = minRate;

                        Platform.runLater(() -> {
                            HomeView.CategoryCardComponent card = view.getCategoryCards().get(type);
                            if (card != null) {
                                card.setPrice(finalRate);

                                if (fxImage != null) {
                                    card.setVehicleImage(fxImage);
                                }

                                // Update specs from database record
                                if (finalRep instanceof Car car) {
                                    card.setSpecs(car.getSeatingCapacity() + " Seats", car.getLuggageCapacityBags() + " Bags", car.getFuelType());
                                } else if (finalRep instanceof Bike bike) {
                                    card.setSpecs(bike.getSeatingCapacity() + " Seats", bike.getMileageKmpl() + " kmpl", bike.getFuelType());
                                } else if (finalRep instanceof SUV suv) {
                                    card.setSpecs(suv.getSeatingCapacity() + " Seats", suv.getLuggageCapacityBags() + " Bags", suv.getFuelType());
                                } else if (finalRep instanceof Van van) {
                                    card.setSpecs(van.getSeatingCapacity() + " Seats", van.getLuggageCapacityBags() + " Bags", van.getFuelType());
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load vehicle records or images from PostgreSQL: " + e.getMessage(), e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}

package com.ready2ride.desktop;

import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.VehicleType;
import com.ready2ride.core.repository.InMemoryBookingRepository;
import com.ready2ride.core.repository.InMemoryVehicleImageRepository;
import com.ready2ride.core.repository.InMemoryVehicleRepository;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.controller.HomeController;
import com.ready2ride.desktop.view.HomeView;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already started
        }
    }

    private void runOnFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX thread timed out");
    }

    @Test
    @DisplayName("Should validate required pickup location")
    void testLocationValidation() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);

            HomeView view = new HomeView();
            new HomeController(view, vehicleService, bookingService, null, type -> {}, () -> {});

            // Clear location
            view.getLocationComboBox().setValue(null);
            view.getSearchVehiclesBtn().fire();

            assertTrue(view.getBookingValidationLabel().isVisible());
            assertTrue(view.getBookingValidationLabel().getText().contains("location"));
        });
    }

    @Test
    @DisplayName("Should reject pickup date in the past")
    void testPastPickupDateValidation() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);

            HomeView view = new HomeView();
            new HomeController(view, vehicleService, bookingService, null, type -> {}, () -> {});

            view.getLocationComboBox().setValue("Mumbai Airport (BOM) — Terminal 2");
            view.getPickupDatePicker().setValue(LocalDate.now().minusDays(2));
            view.getReturnDatePicker().setValue(LocalDate.now().plusDays(2));

            view.getSearchVehiclesBtn().fire();

            assertTrue(view.getBookingValidationLabel().isVisible());
            assertTrue(view.getBookingValidationLabel().getText().contains("past"));
        });
    }

    @Test
    @DisplayName("Should reject return date not strictly after pickup date")
    void testReturnDateValidation() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);

            HomeView view = new HomeView();
            new HomeController(view, vehicleService, bookingService, null, type -> {}, () -> {});

            view.getLocationComboBox().setValue("Mumbai Airport (BOM) — Terminal 2");
            view.getPickupDatePicker().setValue(LocalDate.now().plusDays(5));
            view.getReturnDatePicker().setValue(LocalDate.now().plusDays(3));

            view.getSearchVehiclesBtn().fire();

            assertTrue(view.getBookingValidationLabel().isVisible());
            assertTrue(view.getBookingValidationLabel().getText().contains("Return date must be strictly after pickup"));
        });
    }

    @Test
    @DisplayName("Should successfully forward valid search parameters to catalog navigator")
    void testValidSearchForwarding() throws InterruptedException {
        AtomicBoolean navigated = new AtomicBoolean(false);
        AtomicReference<LocalDate> passedPickup = new AtomicReference<>();
        AtomicReference<LocalDate> passedReturn = new AtomicReference<>();

        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);

            HomeView view = new HomeView();
            new HomeController(view, vehicleService, bookingService, null,
                    type -> {},
                    (type, pickup, returnDate, location) -> {
                        navigated.set(true);
                        passedPickup.set(pickup);
                        passedReturn.set(returnDate);
                    },
                    () -> {});

            LocalDate pickup = LocalDate.now().plusDays(2);
            LocalDate returnDate = LocalDate.now().plusDays(6);

            view.getLocationComboBox().setValue("Pune City Station — Central Hub");
            view.getPickupDatePicker().setValue(pickup);
            view.getReturnDatePicker().setValue(returnDate);

            view.getSearchVehiclesBtn().fire();

            assertFalse(view.getBookingValidationLabel().isVisible());
            assertTrue(navigated.get());
            assertEquals(pickup, passedPickup.get());
            assertEquals(returnDate, passedReturn.get());
        });
    }

    @Test
    @DisplayName("Should toggle between Rent and Find Booking tabs properly")
    void testTabSwitching() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);

            HomeView view = new HomeView();
            new HomeController(view, vehicleService, bookingService, null, type -> {}, () -> {});

            // Initially rent tab is visible
            assertTrue(view.getRentTabContentBox().isVisible());
            assertFalse(view.getFindBookingContentBox().isVisible());

            // Switch to Find Booking
            view.getTabFindBookingBtn().fire();
            assertFalse(view.getRentTabContentBox().isVisible());
            assertTrue(view.getFindBookingContentBox().isVisible());

            // Switch back to Rent
            view.getTabRentVehicleBtn().fire();
            assertTrue(view.getRentTabContentBox().isVisible());
            assertFalse(view.getFindBookingContentBox().isVisible());
        });
    }

    @Test
    @DisplayName("Should lookup existing booking by ID in Find My Booking tab")
    void testBookingLookup() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();

            Car car = new Car("TEST-CAR-1", "Honda", "City", "MH-12-TEST-1", BigDecimal.valueOf(2500), 5, "Petrol");
            vehicleRepo.save(car);

            Booking booking = new Booking("BKG-UNIT-101", "CUST-001", "TEST-CAR-1",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                    BigDecimal.valueOf(5000), BookingStatus.CONFIRMED);
            bookingRepo.save(booking);

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);

            HomeView view = new HomeView();
            new HomeController(view, vehicleService, bookingService, null, type -> {}, () -> {});

            view.getBookingLookupField().setText("BKG-UNIT-101");
            view.getBookingLookupBtn().fire();

            assertTrue(view.getBookingLookupResultLabel().isVisible());
            assertTrue(view.getBookingLookupResultLabel().getText().contains("Reservation Found"));
            assertTrue(view.getBookingLookupResultLabel().getText().contains("Honda City"));
            assertTrue(view.getBookingCancelBtn().isVisible());
        });
    }
}

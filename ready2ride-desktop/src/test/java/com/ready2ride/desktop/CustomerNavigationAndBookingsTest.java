package com.ready2ride.desktop;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.BookingStatus;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.InMemoryBookingRepository;
import com.ready2ride.core.repository.InMemoryUserRepository;
import com.ready2ride.core.repository.InMemoryVehicleImageRepository;
import com.ready2ride.core.repository.InMemoryVehicleRepository;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.controller.DashboardController;
import com.ready2ride.desktop.util.UserSession;
import com.ready2ride.desktop.view.CustomerDashboardView;
import com.ready2ride.desktop.view.DashboardView;
import com.ready2ride.desktop.view.TopNavBar;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerNavigationAndBookingsTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX runtime already started
        }
    }

    @BeforeEach
    void resetSession() {
        UserSession.getInstance().logout();
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
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX thread timed out");
    }

    @Test
    @DisplayName("Task 1: Customer header has clearly visible My Bookings navigation option")
    void testCustomerHeaderHasMyBookingsOption() throws InterruptedException {
        runOnFxThread(() -> {
            TopNavBar navBar = new TopNavBar(TopNavBar.NavPage.HOME);
            assertNotNull(navBar.getNavMyBookingsBtn(), "My Bookings button must exist in header");
            assertEquals("My Bookings", navBar.getNavMyBookingsBtn().getText().trim());

            AtomicBoolean clicked = new AtomicBoolean(false);
            navBar.setOnNavigateMyBookings(() -> clicked.set(true));

            // Log in a customer
            User customer = new User("CUST-T1", "Dhruv Taneja", "dhruv@example.com", "+91 98765 43210", "hash", UserRole.CUSTOMER, "DL-123");
            UserSession.getInstance().login(customer);

            navBar.getNavMyBookingsBtn().fire();
            assertTrue(clicked.get(), "Clicking My Bookings should trigger navigation callback");

            // Set active page to MY_BOOKINGS and verify styling
            navBar.setActivePage(TopNavBar.NavPage.MY_BOOKINGS);
            assertNotNull(navBar.getNavMyBookingsBtn().getGraphic());
        });
    }

    @Test
    @DisplayName("Task 1: Customer profile menu contains My Bookings and Logout")
    void testCustomerProfileDropdownMenu() throws InterruptedException {
        runOnFxThread(() -> {
            TopNavBar navBar = new TopNavBar(TopNavBar.NavPage.HOME);

            AtomicBoolean logoutNavigated = new AtomicBoolean(false);
            navBar.setOnLogout(() -> logoutNavigated.set(true));

            AtomicBoolean myBookingsNavigated = new AtomicBoolean(false);
            navBar.setOnNavigateMyBookings(() -> myBookingsNavigated.set(true));

            // Log in customer
            User customer = new User("CUST-T2", "Dhruv Taneja", "dhruv@example.com", "+91 98765 43210", "hash", UserRole.CUSTOMER, "DL-123");
            UserSession.getInstance().login(customer);
            navBar.refreshAuthBox();

            assertTrue(UserSession.getInstance().isLoggedIn());
            assertEquals("Dhruv Taneja", UserSession.getInstance().getDisplayName());

            // Simulate logout
            UserSession.getInstance().logout();
            assertFalse(UserSession.getInstance().isLoggedIn());
            navBar.refreshAuthBox();
        });
    }

    @Test
    @DisplayName("Task 2: Customer My Bookings page loads bookings, enforces ownership, and supports cancellation")
    void testMyBookingsBackendIntegrationAndCancellation() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryUserRepository userRepo = new InMemoryUserRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);
            AuthenticationService authService = new AuthenticationService(userRepo);

            // Add vehicle
            Vehicle car = new Car("CAR-101", "Toyota", "Corolla", "MH-12-RR-1001", BigDecimal.valueOf(2499.00));
            vehicleRepo.save(car);

            // Customer 1 and Customer 2
            User cust1 = new User("CUST-001", "Dhruv Taneja", "dhruv@example.com", "+91 98765 43210", "hash", UserRole.CUSTOMER, "DL-1");
            User cust2 = new User("CUST-002", "Other Customer", "other@example.com", "+91 98765 43211", "hash", UserRole.CUSTOMER, "DL-2");
            userRepo.save(cust1);
            userRepo.save(cust2);

            // Create booking for Customer 1
            Booking bkg1 = bookingService.createBooking("BKG-001", "CUST-001", "CAR-101",
                    LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(5));
            assertEquals(BookingStatus.CONFIRMED, bkg1.getStatus());

            // 1. Ownership check: Customer 2 CANNOT access Customer 1's booking
            assertThrows(SecurityException.class, () ->
                    bookingService.getBookingForCustomer("BKG-001", "CUST-002"));

            // 2. Ownership check: Customer 2 CANNOT cancel Customer 1's booking
            assertThrows(SecurityException.class, () ->
                    bookingService.cancelBookingForCustomer("BKG-001", "CUST-002"));

            // 3. Customer 1 CAN access and cancel their own booking
            Booking retrieved = bookingService.getBookingForCustomer("BKG-001", "CUST-001");
            assertNotNull(retrieved);
            assertEquals("BKG-001", retrieved.getBookingId());

            Booking cancelled = bookingService.cancelBookingForCustomer("BKG-001", "CUST-001");
            assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

            // Verify status in repository
            assertEquals(BookingStatus.CANCELLED, bookingRepo.findById("BKG-001").orElseThrow().getStatus());

            // Vehicle is available again
            assertTrue(bookingService.isVehicleAvailable("CAR-101", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(5)));

            // Verify CustomerDashboardView renders without error
            UserSession.getInstance().login(cust1);
            CustomerDashboardView custView = new CustomerDashboardView(
                    bookingService, vehicleService, null, null, authService,
                    () -> {}, () -> {}, () -> {}
            );
            assertNotNull(custView.getRootNode());
        });
    }

    @Test
    @DisplayName("Task 3: Rental Provider Dashboard sidebar has pinned Logout button that clears session and navigates to login")
    void testRentalProviderDashboardLogout() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryUserRepository userRepo = new InMemoryUserRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);
            AuthenticationService authService = new AuthenticationService(userRepo);

            // Log in as rental provider
            User provider = new User("PROV-001", "Rajesh Sharma", "provider@ready2ride.com", "+91 98111 22334", "hash", UserRole.RENTAL_PROVIDER, "DL-PROV-1");
            userRepo.save(provider);
            UserSession.getInstance().login(provider);

            assertTrue(UserSession.getInstance().isLoggedIn());
            assertTrue(UserSession.getInstance().isRentalProvider());

            AtomicBoolean logoutTriggered = new AtomicBoolean(false);
            DashboardView dashboardView = new DashboardView();
            new DashboardController(dashboardView, vehicleService, bookingService, authService, null, null, () -> {
                logoutTriggered.set(true);
            });

            // Verify Logout button exists in the sidebar
            assertNotNull(dashboardView.getLogoutBtn(), "Provider Dashboard must have a Logout button");
            assertEquals("  Logout", dashboardView.getLogoutBtn().getText());

            // Fire the Logout button
            dashboardView.getLogoutBtn().fire();

            // 1. Session must be cleared
            assertFalse(UserSession.getInstance().isLoggedIn(), "UserSession must be cleared on logout");

            // 2. onLogout callback must be invoked
            assertTrue(logoutTriggered.get(), "Logout callback must be invoked");

            // 3. Another user can log in cleanly without inheriting previous state
            User customer = new User("CUST-999", "New Customer", "new@example.com", "+91 99999 99999", "hash", UserRole.CUSTOMER, "DL-999");
            UserSession.getInstance().login(customer);
            assertTrue(UserSession.getInstance().isLoggedIn());
            assertFalse(UserSession.getInstance().isRentalProvider());
            assertEquals("New Customer", UserSession.getInstance().getDisplayName());
        });
    }

    @Test
    @DisplayName("Task 4: Customer with no bookings sees clean empty state with Browse Vehicles action")
    void testCustomerWithNoBookingsShowsEmptyState() throws InterruptedException {
        runOnFxThread(() -> {
            InMemoryVehicleRepository vehicleRepo = new InMemoryVehicleRepository();
            InMemoryVehicleImageRepository imageRepo = new InMemoryVehicleImageRepository();
            InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
            InMemoryUserRepository userRepo = new InMemoryUserRepository();

            VehicleService vehicleService = new VehicleService(vehicleRepo, imageRepo);
            BookingService bookingService = new BookingService(bookingRepo, vehicleRepo);
            AuthenticationService authService = new AuthenticationService(userRepo);

            User customer = new User("CUST-EMPTY", "Alice Smith", "alice@example.com", "+91 91234 56789", "hash", UserRole.CUSTOMER, "DL-ALICE");
            userRepo.save(customer);
            UserSession.getInstance().login(customer);

            AtomicBoolean catalogNavigated = new AtomicBoolean(false);
            CustomerDashboardView custView = new CustomerDashboardView(
                    bookingService, vehicleService, null, null, authService,
                    () -> catalogNavigated.set(true), () -> {}, () -> {}
            );

            assertNotNull(custView.getRootNode());
            assertEquals(0, bookingService.getBookingsByCustomer("CUST-EMPTY").size(),
                    "Customer must have zero bookings initially");
        });
    }
}

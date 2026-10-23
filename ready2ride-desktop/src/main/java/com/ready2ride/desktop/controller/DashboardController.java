package com.ready2ride.desktop.controller;

import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.MaintenanceService;
import com.ready2ride.core.service.PaymentService;
import com.ready2ride.core.service.VehicleService;
import com.ready2ride.desktop.view.DashboardView;

import java.util.Objects;

/**
 * Controller managing navigation and live administration within the Ready2Ride Admin Console.
 * Directly links the Administrator UI to live PostgreSQL services for:
 * - Fleet vehicle catalog & binary image upload
 * - Customer reservations & booking status lifecycle
 * - User and customer directory management
 * - Payment transaction records
 * - Scheduled vehicle maintenance
 */
public class DashboardController {

    private final DashboardView view;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final AuthenticationService authService;
    private final PaymentService paymentService;
    private final MaintenanceService maintenanceService;
    private final Runnable onLogout;

    public DashboardController(DashboardView view,
                               VehicleService vehicleService,
                               BookingService bookingService,
                               AuthenticationService authService,
                               PaymentService paymentService,
                               MaintenanceService maintenanceService,
                               Runnable onLogout) {
        this.view = Objects.requireNonNull(view, "DashboardView must not be null");
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.authService = authService;
        this.paymentService = paymentService;
        this.maintenanceService = maintenanceService;
        this.onLogout = Objects.requireNonNull(onLogout, "Logout callback must not be null");

        initEventHandlers();
        showDashboard();
    }

    public DashboardController(DashboardView view, VehicleService vehicleService, Runnable onLogout) {
        this(view, vehicleService, null, null, null, null, onLogout);
    }

    public DashboardController(DashboardView view, Runnable onLogout) {
        this(view, null, null, null, null, null, onLogout);
    }

    private void initEventHandlers() {
        // Overview Dashboard
        view.getNavDashboardBtn().setOnAction(e -> showDashboard());

        // My Vehicles (Live Provider Inventory)
        view.getNavVehiclesBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavVehiclesBtn());
            if (vehicleService != null) {
                view.showLiveVehiclesManagement(vehicleService, this::showDashboard);
            }
        });

        // Add Vehicle
        view.getNavAddVehicleBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavAddVehicleBtn());
            if (vehicleService != null) {
                view.showAddVehicleView(vehicleService, this::showDashboard);
            }
        });

        // Provider Bookings
        view.getNavBookingsBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavBookingsBtn());
            if (bookingService != null) {
                view.showLiveBookingsManagement(bookingService, vehicleService, this::showDashboard);
            }
        });

        // Provider Profile & Settings
        view.getNavProfileBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavProfileBtn());
            if (authService != null) {
                view.showProfileManagement(authService, this::showDashboard);
            }
        });

        // Logout Action (clears session and returns to login)
        view.getLogoutBtn().setOnAction(e -> {
            view.clearCachedData();
            com.ready2ride.desktop.util.UserSession.getInstance().logout();
            if (onLogout != null) {
                onLogout.run();
            }
        });
    }

    private void showDashboard() {
        view.updateActiveNavButton(view.getNavDashboardBtn());
        if (vehicleService != null || bookingService != null || maintenanceService != null) {
            view.renderLiveDashboardOverview(vehicleService, bookingService, maintenanceService, this::showDashboard);
        } else {
            view.restoreDashboardContent();
        }
    }
}

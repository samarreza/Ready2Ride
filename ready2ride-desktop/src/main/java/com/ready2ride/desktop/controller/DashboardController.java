package com.ready2ride.desktop.controller;

import com.ready2ride.desktop.view.DashboardView;

import java.util.Objects;

/**
 * Controller managing navigation and interactions within the Ready2Ride Dashboard.
 * Handles module preview tabs, dashboard restoration, and logout navigation.
 */
public class DashboardController {

    private final DashboardView view;
    private final Runnable onLogout;

    public DashboardController(DashboardView view, Runnable onLogout) {
        this.view = Objects.requireNonNull(view, "DashboardView must not be null");
        this.onLogout = Objects.requireNonNull(onLogout, "Logout callback must not be null");

        initEventHandlers();
    }

    private void initEventHandlers() {
        // Dashboard Tab
        view.getNavDashboardBtn().setOnAction(e -> showDashboard());

        // Vehicles Module Preview
        view.getNavVehiclesBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavVehiclesBtn());
            view.showModulePreview(
                    "Fleet Vehicles",
                    "The Vehicles module will support comprehensive fleet management, including adding, editing, and retiring vehicles across all subclasses (Cars, Bikes, SUVs, Vans) with configurable rate tiers and availability toggles.",
                    this::showDashboard
            );
        });

        // Bookings Module Preview
        view.getNavBookingsBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavBookingsBtn());
            view.showModulePreview(
                    "Reservations & Bookings",
                    "The Bookings module will provide an interactive reservation calendar, schedule conflict analysis, row-locked database overlap enforcement, and cancellation workflows.",
                    this::showDashboard
            );
        });

        // Customers Module Preview
        view.getNavCustomersBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavCustomersBtn());
            view.showModulePreview(
                    "Customer Directory",
                    "The Customers directory will maintain registered renter accounts, contact details, rental histories, and driver's license verification credentials.",
                    this::showDashboard
            );
        });

        // Payments Module Preview
        view.getNavPaymentsBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavPaymentsBtn());
            view.showModulePreview(
                    "Payments & Invoicing",
                    "The Payments module will provide invoice auditing, transaction tracking across multiple payment methods (UPI, Card, Cash), and financial ledger reporting.",
                    this::showDashboard
            );
        });

        // Maintenance Module Preview
        view.getNavMaintenanceBtn().setOnAction(e -> {
            view.updateActiveNavButton(view.getNavMaintenanceBtn());
            view.showModulePreview(
                    "Fleet Maintenance",
                    "The Maintenance module will track routine servicing, repair histories, maintenance costs, and vehicle safety certification checkpoints.",
                    this::showDashboard
            );
        });

        // Logout Action
        view.getLogoutBtn().setOnAction(e -> onLogout.run());
    }

    private void showDashboard() {
        view.updateActiveNavButton(view.getNavDashboardBtn());
        view.restoreDashboardContent();
    }
}

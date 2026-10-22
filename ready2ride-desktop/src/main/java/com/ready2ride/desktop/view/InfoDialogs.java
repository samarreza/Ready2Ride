package com.ready2ride.desktop.view;

import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Informational dialog presentation for "About Us" and "Contact".
 */
public class InfoDialogs {

    public static void showAboutUs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Ready2Ride");
        alert.setHeaderText("Ready2Ride — Vehicle Rental Management System");

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        Label l1 = new Label("Ready2Ride is a next-generation fleet mobility platform offering self-drive cars, bikes, adventure SUVs, and passenger vans.");
        l1.setWrapText(true);
        Label l2 = new Label("• Fleet: Over 500+ sanitized, verified vehicles across major metro airports and railway hubs.");
        Label l3 = new Label("• Transparent Pricing: Fixed-precision rates in INR with zero hidden surcharges.");
        Label l4 = new Label("• Safety First: Routine maintenance inspections, 24/7 roadside assistance, and sanitized interiors.");
        box.getChildren().addAll(l1, l2, l3, l4);

        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    public static void showWhyChooseUs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Why Choose Ready2Ride");
        alert.setHeaderText("Why Thousands of Riders Trust Ready2Ride Daily");

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        Label l1 = new Label("• 100% Verified Fleet: Every vehicle undergoes rigorous 50-point technical inspections.");
        Label l2 = new Label("• Best Rate Guarantee: Direct-from-depot pricing with zero hidden surcharges.");
        Label l3 = new Label("• 24/7 Instant Support: Live customer care and nationwide emergency roadside assistance.");
        Label l4 = new Label("• Seamless Booking: Fast digital reservations with instant booking confirmation.");
        box.getChildren().addAll(l1, l2, l3, l4);

        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    public static void showContact() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Ready2Ride");
        alert.setHeaderText("Get In Touch With Our 24/7 Support Team");

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        Label l1 = new Label("We're here for you anytime, day or night.");
        Label l2 = new Label("📞 Toll-Free Helpline: +91-1800-RIDE-NOW (1800-7433-669)");
        Label l3 = new Label("✉ Email: support@ready2ride.internal / bookings@ready2ride.internal");
        Label l4 = new Label("📍 Headquarters: Ready2Ride Mobility Tower, Cyber City, Mumbai & Bengaluru");
        Label l5 = new Label("🕒 Operating Hours: 24 Hours / 7 Days a week");
        box.getChildren().addAll(l1, l2, l3, l4, l5);

        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    public static void showAboutUsDialog() {
        showAboutUs();
    }

    public static void showWhyChooseUsDialog() {
        showWhyChooseUs();
    }

    public static void showContactDialog() {
        showContact();
    }
}

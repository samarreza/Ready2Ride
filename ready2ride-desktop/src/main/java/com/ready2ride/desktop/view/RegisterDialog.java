package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Customer;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.UUID;

/**
 * Dialog enabling customer registration in PostgreSQL with secure bcrypt hashing.
 */
public class RegisterDialog {

    public static void show(AuthenticationService authService) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Ready2Ride — Customer Registration");
        dialog.setHeaderText("Create Your Ready2Ride Customer Account\nInstant access to self-drive car, bike, and SUV rentals.");

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setPrefWidth(380);

        Label nameLabel = new Label("Full Name *");
        TextField nameField = UiTheme.createStyledTextField("e.g. Aditi Sharma");

        Label emailLabel = new Label("Email Address *");
        TextField emailField = UiTheme.createStyledTextField("aditi@example.com");

        Label phoneLabel = new Label("Phone Number *");
        TextField phoneField = UiTheme.createStyledTextField("+91-9876543210");

        Label licenseLabel = new Label("Driving License Number *");
        TextField licenseField = UiTheme.createStyledTextField("DL-MH-2024-0019283");

        Label passLabel = new Label("Password (minimum 8 characters) *");
        PasswordField passField = UiTheme.createStyledPasswordField("••••••••");

        Label errorLabel = new Label();
        errorLabel.setFont(UiTheme.FONT_BODY);
        errorLabel.setTextFill(UiTheme.COLOR_ERROR);
        errorLabel.setWrapText(true);

        content.getChildren().addAll(
                nameLabel, nameField,
                emailLabel, emailField,
                phoneLabel, phoneField,
                licenseLabel, licenseField,
                passLabel, passField,
                errorLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Register Account");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText() != null ? nameField.getText().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
            String license = licenseField.getText() != null ? licenseField.getText().trim() : "";
            String pass = passField.getText() != null ? passField.getText() : "";

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || license.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please fill in all mandatory fields.");
                event.consume();
                return;
            }

            if (pass.length() < 8) {
                errorLabel.setText("Password must be at least 8 characters long.");
                event.consume();
                return;
            }

            try {
                com.ready2ride.core.model.User user = authService.registerCustomer(name, email, phone, pass, license);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText("Welcome to Ready2Ride, " + user.getFullName() + "!");
                alert.setContentText("Your account has been created with Customer ID: " + user.getId() + "\nYou can now reserve vehicles.");
                alert.showAndWait();
            } catch (Exception ex) {
                errorLabel.setText("Registration error: " + ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}

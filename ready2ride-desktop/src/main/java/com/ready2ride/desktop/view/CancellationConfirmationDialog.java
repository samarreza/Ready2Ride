package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Custom JavaFX modal confirmation dialog for Ready2Ride booking cancellation.
 * Replaces the OS/Windows default confirmation dialog with the unified Ready2Ride
 * blue-and-white theme, rounded buttons, clear typography, and booking summary.
 */
public final class CancellationConfirmationDialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private CancellationConfirmationDialog() {
        // Prevent direct instantiation
    }

    /**
     * Displays the custom cancellation confirmation modal.
     *
     * @param ownerWindow parent window to attach modality
     * @param booking     the booking to cancel
     * @param vehicle     associated vehicle model (optional, may be null)
     * @return true if user clicked 'Confirm Cancellation', false if user clicked 'Keep Booking' or closed
     */
    public static boolean show(Window ownerWindow, Booking booking, Vehicle vehicle) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Cancel Reservation — " + booking.getBookingId());
        if (ownerWindow != null) {
            dialog.initOwner(ownerWindow);
        }

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtnLookup = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtnLookup != null) {
            closeBtnLookup.setVisible(false);
            closeBtnLookup.setManaged(false);
        }

        VBox contentBox = new VBox(16);
        contentBox.setPrefWidth(500);
        contentBox.setMinWidth(480);
        contentBox.setMaxWidth(540);
        contentBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        contentBox.setPadding(new Insets(24, 28, 24, 28));

        // --- Top Bar: Icon + Title + 'X' Close ---
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(44, 44);
        iconCircle.setMinSize(44, 44);
        iconCircle.setMaxSize(44, 44);
        iconCircle.setBackground(UiTheme.roundedBackground(Color.web("#FEE2E2"), 22));
        iconCircle.setBorder(UiTheme.roundedBorder(Color.web("#FECACA"), 22, 1));

        SVGPath alertSvg = new SVGPath();
        alertSvg.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
        alertSvg.setFill(Color.web("#DC2626"));
        alertSvg.setScaleX(1.1);
        alertSvg.setScaleY(1.1);
        iconCircle.getChildren().add(alertSvg);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Cancel Reservation");
        titleLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        titleLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label subLabel = new Label("Review your booking details before confirming cancellation");
        subLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 12));
        subLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        titleBox.getChildren().addAll(titleLabel, subLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button xCloseBtn = new Button("✕");
        xCloseBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 14));
        xCloseBtn.setTextFill(UiTheme.COLOR_TEXT_HINT);
        xCloseBtn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
        xCloseBtn.setBorder(javafx.scene.layout.Border.EMPTY);
        xCloseBtn.setCursor(Cursor.HAND);
        xCloseBtn.setOnAction(e -> {
            dialog.setResult(Boolean.FALSE);
            dialog.close();
        });
        xCloseBtn.setOnMouseEntered(e -> xCloseBtn.setTextFill(UiTheme.COLOR_NAVY_DARK));
        xCloseBtn.setOnMouseExited(e -> xCloseBtn.setTextFill(UiTheme.COLOR_TEXT_HINT));

        header.getChildren().addAll(iconCircle, titleBox, headerSpacer, xCloseBtn);

        // Divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setBackground(UiTheme.fillBackground(UiTheme.COLOR_BORDER_LIGHT));

        // --- Booking Summary Card ---
        VBox summaryBox = new VBox(10);
        summaryBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 10));
        summaryBox.setBorder(UiTheme.roundedBorder(UiTheme.COLOR_BORDER_LIGHT, 10, 1));
        summaryBox.setPadding(new Insets(14, 16, 14, 16));

        // Ref row
        HBox refRow = new HBox(8);
        refRow.setAlignment(Pos.CENTER_LEFT);
        Label refTitle = new Label("BOOKING REFERENCE");
        refTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 11));
        refTitle.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Region refSpacer = new Region();
        HBox.setHgrow(refSpacer, Priority.ALWAYS);

        Label statusBadge = UiTheme.createStatusBadge(
                booking.getStatus() != null ? booking.getStatus().name() : "CONFIRMED",
                booking.getStatus() != null && booking.getStatus().name().equalsIgnoreCase("CONFIRMED")
                        ? Color.web("#15803D") : Color.web("#B45309"),
                booking.getStatus() != null && booking.getStatus().name().equalsIgnoreCase("CONFIRMED")
                        ? Color.web("#DCFCE7") : Color.web("#FEF3C7"),
                booking.getStatus() != null && booking.getStatus().name().equalsIgnoreCase("CONFIRMED")
                        ? Color.web("#86EFAC") : Color.web("#FCD34D")
        );
        refRow.getChildren().addAll(refTitle, refSpacer, statusBadge);

        Label refVal = new Label(booking.getBookingId());
        refVal.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        refVal.setTextFill(UiTheme.COLOR_PRIMARY);

        // Vehicle Info
        String vehicleName = vehicle != null ? (vehicle.getBrand() + " " + vehicle.getModel()) : "Reserved Vehicle";
        String reg = (vehicle != null && vehicle.getRegistrationNumber() != null)
                ? "  (" + vehicle.getRegistrationNumber() + ")" : "";
        Label vehLabel = new Label(vehicleName + reg);
        vehLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        vehLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        // Dates Info
        String pickupStr = booking.getPickupDateTime() != null ? booking.getPickupDateTime().format(DATE_FORMATTER) : "N/A";
        String returnStr = booking.getReturnDateTime() != null ? booking.getReturnDateTime().format(DATE_FORMATTER) : "N/A";
        Label datesLabel = new Label("Pickup:  " + pickupStr + "\nReturn:  " + returnStr);
        datesLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 12));
        datesLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        // Amount Row
        HBox amountRow = new HBox(8);
        amountRow.setAlignment(Pos.CENTER_LEFT);
        Label amtLabel = new Label("Total Rental Price:");
        amtLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 13));
        amtLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        Region amtSpacer = new Region();
        HBox.setHgrow(amtSpacer, Priority.ALWAYS);

        Label amtVal = new Label(String.format("₹ %,.2f",
                booking.getRentalAmount() != null ? booking.getRentalAmount().doubleValue() : 0.0));
        amtVal.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        amtVal.setTextFill(UiTheme.COLOR_NAVY_DARK);
        amountRow.getChildren().addAll(amtLabel, amtSpacer, amtVal);

        summaryBox.getChildren().addAll(refRow, refVal, vehLabel, datesLabel, amountRow);

        // --- Notice Alert Box (Soft Red Theme) ---
        HBox noticeBox = new HBox(10);
        noticeBox.setAlignment(Pos.CENTER_LEFT);
        noticeBox.setBackground(UiTheme.roundedBackground(Color.web("#FEF2F2"), 8));
        noticeBox.setBorder(UiTheme.roundedBorder(Color.web("#FECACA"), 8, 1));
        noticeBox.setPadding(new Insets(10, 14, 10, 14));

        Label noticeIcon = new Label("⚠");
        noticeIcon.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        noticeIcon.setTextFill(Color.web("#DC2626"));

        Label noticeText = new Label("Are you sure you want to cancel this booking? This will release your reservation in PostgreSQL and make the vehicle available for other customers.");
        noticeText.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 12));
        noticeText.setTextFill(Color.web("#991B1B"));
        noticeText.setWrapText(true);
        HBox.setHgrow(noticeText, Priority.ALWAYS);

        noticeBox.getChildren().addAll(noticeIcon, noticeText);

        // --- Action Buttons: Keep Booking vs Confirm Cancellation ---
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(6, 0, 0, 0));

        // Keep Booking Action (Closes dialog with no modification)
        Button keepBtn = new Button("Keep Booking");
        keepBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        keepBtn.setTextFill(Color.web("#334155"));
        keepBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        keepBtn.setBorder(UiTheme.roundedBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1.2));
        keepBtn.setPadding(new Insets(10, 20, 10, 20));
        keepBtn.setCursor(Cursor.HAND);
        keepBtn.setOnAction(e -> {
            dialog.setResult(Boolean.FALSE);
            dialog.close();
        });
        keepBtn.setOnMouseEntered(e -> {
            keepBtn.setBackground(UiTheme.roundedBackground(Color.web("#F1F5F9"), 8));
            keepBtn.setBorder(UiTheme.roundedBorder(Color.web("#94A3B8"), 8, 1.2));
        });
        keepBtn.setOnMouseExited(e -> {
            keepBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            keepBtn.setBorder(UiTheme.roundedBorder(UiTheme.COLOR_BORDER_INPUT, 8, 1.2));
        });

        // Confirm Cancellation Action (Proceeds with backend cancellation)
        Button confirmBtn = new Button("Confirm Cancellation");
        confirmBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 13));
        confirmBtn.setTextFill(UiTheme.COLOR_WHITE);
        confirmBtn.setBackground(UiTheme.roundedBackground(Color.web("#EF4444"), 8));
        confirmBtn.setBorder(javafx.scene.layout.Border.EMPTY);
        confirmBtn.setPadding(new Insets(10, 22, 10, 22));
        confirmBtn.setCursor(Cursor.HAND);
        confirmBtn.setOnAction(e -> {
            dialog.setResult(Boolean.TRUE);
            dialog.close();
        });
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setBackground(UiTheme.roundedBackground(Color.web("#DC2626"), 8)));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setBackground(UiTheme.roundedBackground(Color.web("#EF4444"), 8)));

        btnRow.getChildren().addAll(keepBtn, confirmBtn);

        contentBox.getChildren().addAll(header, div, summaryBox, noticeBox, btnRow);
        dialogPane.setContent(contentBox);

        dialog.setResultConverter(buttonType -> dialog.getResult());

        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(Boolean.FALSE);
    }

    /**
     * Shows a customized notification dialog (Success or Error) with Ready2Ride design.
     *
     * @param ownerWindow parent window
     * @param title       dialog title
     * @param headerText  dialog header
     * @param message     dialog detailed message
     * @param isSuccess   true for green success, false for red error
     */
    public static void showNotification(Window ownerWindow, String title, String headerText, String message, boolean isSuccess) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        if (ownerWindow != null) {
            dialog.initOwner(ownerWindow);
        }

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtnLookup = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtnLookup != null) {
            closeBtnLookup.setVisible(false);
            closeBtnLookup.setManaged(false);
        }

        VBox box = new VBox(16);
        box.setPrefWidth(440);
        box.setMinWidth(400);
        box.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 16));
        box.setPadding(new Insets(24, 26, 22, 26));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(42, 42);
        iconCircle.setMinSize(42, 42);
        iconCircle.setMaxSize(42, 42);
        Color circleBg = isSuccess ? Color.web("#DCFCE7") : Color.web("#FEE2E2");
        Color circleBorder = isSuccess ? Color.web("#86EFAC") : Color.web("#FECACA");
        iconCircle.setBackground(UiTheme.roundedBackground(circleBg, 21));
        iconCircle.setBorder(UiTheme.roundedBorder(circleBorder, 21, 1));

        SVGPath iconSvg = new SVGPath();
        if (isSuccess) {
            // Checkmark SVG
            iconSvg.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
            iconSvg.setFill(Color.web("#16A34A"));
        } else {
            // Cross SVG
            iconSvg.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
            iconSvg.setFill(Color.web("#DC2626"));
        }
        iconSvg.setScaleX(1.1);
        iconSvg.setScaleY(1.1);
        iconCircle.getChildren().add(iconSvg);

        VBox textCol = new VBox(2);
        Label headerLabel = new Label(headerText);
        headerLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 16));
        headerLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label sub = new Label(isSuccess ? "Ready2Ride Operation Completed" : "Operation Could Not Be Completed");
        sub.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 12));
        sub.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        textCol.getChildren().addAll(headerLabel, sub);

        topRow.getChildren().addAll(iconCircle, textCol);

        Label bodyMsg = new Label(message);
        bodyMsg.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.NORMAL, 13));
        bodyMsg.setTextFill(UiTheme.COLOR_TEXT_DARK);
        bodyMsg.setWrapText(true);

        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button okBtn = UiTheme.createPrimaryButton("Dismiss");
        okBtn.setOnAction(e -> dialog.close());
        btnRow.getChildren().add(okBtn);

        box.getChildren().addAll(topRow, bodyMsg, btnRow);
        dialogPane.setContent(box);

        dialog.showAndWait();
    }
}
